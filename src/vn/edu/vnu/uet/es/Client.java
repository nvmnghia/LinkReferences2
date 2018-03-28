package vn.edu.vnu.uet.es;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.util.Pair;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import vn.edu.vnu.uet.bitap.Bitap;
import vn.edu.vnu.uet.config.Config;
import vn.edu.vnu.uet.data.Article;
import vn.edu.vnu.uet.data.References;
import vn.edu.vnu.uet.data.Result;
import vn.edu.vnu.uet.util.DataUtl;
import vn.edu.vnu.uet.util.StringUtl;

import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class Client {
    public static void main(String[] args) throws SQLException, UnknownHostException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrayList<Result> results = new ArrayList<>(args.length);

        // Shuffle, just for testing
        Random random = new Random();
        for (int i = args.length - 1; i > 0; --i) {
            int index = random.nextInt(i + 1);

            // Simple swap
            String s = args[index];
            args[index] = args[i];
            args[i] = s;
        }

        for (String id : args) {
            Result result = getResults(id);

            if (result != null) {
                results.add(result);
                System.out.println(gson.toJson(result));
            }
        }

//        System.out.println(gson.toJson(results));
    }

    /**
     * Get the search result
     *
     * @param articleID
     * @return Result: id and its score
     * @throws UnknownHostException
     * @throws SQLException
     */
    protected static Result getResults(String articleID) throws UnknownHostException, SQLException {
        Article article = getArticle(articleID);
        if (article == null) {
            return null;
        }

        Result result = new Result();
        result.id = article.getId();

        ArrayList<Pair<Article, Float>> candidates = filterByTitle(article);
        candidates = filterByAuthors(article, candidates);

        for (Pair<Article, Float> candidate : candidates) {
            result.results.put(candidate.getKey().getId(), candidate.getValue());
        }

        return result;
    }

    /**
     * Get Article from DB
     *
     * @param id id of the article
     * @return Article
     * @throws SQLException whenever Connector/J goes nut
     */
    private static Article getArticle(String id) throws SQLException {
        String query = "SELECT ar.id, ar.title, ar.reference, ar.doi, GROUP_CONCAT(au.name SEPARATOR ', ') FROM articles ar "
                + "JOIN articles_authors aa ON aa.article_id = ar.id "
                + "JOIN authors au ON aa.author_id = au.id "
                + "WHERE ar.is_vci = 1 AND ar.reference IS NOT NULL AND length(ar.reference) >= 1 AND ar.id = " + id;

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Statement statement = connection.createStatement();
        statement.executeQuery("USE " + Config.DB_NAME);

        // Query
        ResultSet articleSet = statement.executeQuery(query);
        Article article = null;

        if (articleSet.next() && articleSet.getString(3) != null) {
            article = new Article();
            article.setId(articleSet.getInt(1));
            article.setTitle(StringUtl.clean(articleSet.getString(2)));
            article.setReferences(new References(StringUtl.clean(articleSet.getString(3))));
            article.setDOI(articleSet.getString(4));
            article.setAuthors(StringUtl.clean(articleSet.getString(5)));
        }

        // Carefully close everything
        try { if (articleSet != null) articleSet.close(); } catch (Exception e) {}
        try { if (statement != null) statement.close(); } catch (Exception e) {}
        try { if (connection != null) connection.close(); } catch (Exception e) {}

        return article;
    }

    /**
     * ES stores all the references
     * This function queries ES for references that may contain the input title
     * Boolean model makes ES search results fluctuate too much
     * Therefore another check must be applied
     * Bitap is used to compare the result title to the references
     *
     * @param article The article whose title will be searched for
     * @return candidates The possible articles, along with theirs score
     * @throws UnknownHostException whenever ES goes nuts
     */
    private static ArrayList<Pair<Article, Float>> filterByTitle(Article article) throws UnknownHostException {
        MatchQueryBuilder builder = matchQuery("reference", article.getTitle());
        SearchHits hits = DataUtl.queryES(Config.ES_INDEX, builder);
        ArrayList<Pair<Article, Float>> candidates = new ArrayList<>();

        for (SearchHit hit : hits) {
            String rawReference = (String) hit.getSourceAsMap().get("reference");
            float[] bitapResult = Bitap.match(rawReference, article.getTitle(), 0.1f);

//            if (bitapResult[0] != -1) {
//                // Found
//                Article candidate = new Article();
//                candidate.setId(Integer.valueOf(hit.getId()));
//                candidate.setAuthors((String) hit.getSourceAsMap().get("authors"));
//                candidate.setReferences(new References(rawReference));
//
//                // candidate's title is not needed anymore
//                // article.setTitle((String) hit.getSourceAsMap().get("title"));
//
//                candidates.add(new Pair<>(candidate, bitapResult[1]));
//            }

            if (bitapResult[0] == -1) {
                // Not found, just skip
                continue;
            }

            // Found, now drill down to find the exact reference line
            String[] references = new References(rawReference).getSeparatedReferences();
            for (String reference : references) {
                bitapResult = Bitap.match(reference, article.getTitle(), 0.1f);
                if (bitapResult[0] != -1) {
                    // Found exact reference line
                    Article candidate = new Article();
                    candidate.setId(Integer.valueOf(hit.getId()));
                    candidate.setAuthors((String) hit.getSourceAsMap().get("authors"));
                    candidate.setReferences(new References(reference));

                    // candidate's title is not needed anymore
                    // article.setTitle((String) hit.getSourceAsMap().get("title"));

                    candidates.add(new Pair<>(candidate, bitapResult[1]));
                }
            }
        }

        return candidates;
    }

    /**
     * Use author information to refine results
     * Articles are removed if the overall accuracy is lower than 0.5f
     *
     * @param article The article whose author will be searched for
     * @param candidates The possible articles
     * @return
     */
    private static ArrayList<Pair<Article, Float>> filterByAuthors(Article article, ArrayList<Pair<Article, Float>> candidates) {
        ArrayList<ArrayList<String>> authors = article.getPossibleAuthorNames();

        for (int i = 0; i < candidates.size(); ++i) {
            Article candidate = candidates.get(i).getKey();
            float titleScore = candidates.get(i).getValue();

            String reference = candidate.getReferences().getRaw().toLowerCase();
            int matchedAuthors = 0;

            for (ArrayList<String> author : authors) {
                for (String possibleName : author) {
                    // Normal contains
//                    if (reference.contains(possibleName)) {
//                        ++matchedAuthors;
//                        break;
//                    }

                    // Overkill contains
                    if (Bitap.isMatch(reference, possibleName, 1 / possibleName.length())) {
                        // Allow at most 1 error
                        ++matchedAuthors;
                        break;
                    }
                }
            }

            float authorScore = ((float) matchedAuthors) / authors.size();
            if (authorScore < 0.5f && matchedAuthors != 0 && (reference.contains("nnk") || reference.contains("et al"))) {
                // Boost score for "et al" cases
                authorScore = 0.5f;
            }

            float overallScore = (authorScore + titleScore) / 2;
            if (overallScore < 0.7f) {
                // Too low score, remove
                // Generally speaking, author score is much lower than title score,
                // so the threshold is set this low to tolerate it
                candidates.remove(i);
                --i;
            }
        }

        return candidates;
    }
}
