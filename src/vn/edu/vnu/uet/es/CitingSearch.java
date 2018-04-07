package vn.edu.vnu.uet.es;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.util.Pair;
import vn.edu.vnu.uet.bitap.Bitap;
import vn.edu.vnu.uet.config.Config;
import vn.edu.vnu.uet.data.Article;
import vn.edu.vnu.uet.data.References;
import vn.edu.vnu.uet.data.Result;
import vn.edu.vnu.uet.util.BitSetUtl;
import vn.edu.vnu.uet.util.DataUtl;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Given an article ID, search for documents that it cites
 */

public class CitingSearch {


    /**
     * Get the search results.
     * Each result includes found article's ID, the given article ID and its scores
     *
     * @param articleID
     * @return Result: Article IDs and theirs scores
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    protected static ArrayList<Result> getResults(String articleID) throws UnsupportedEncodingException, URISyntaxException {
        Article article = CitationSearch.getArticleByID(articleID);
        if (article == null || article.getReferences().getRaw() == null || article.getReferences().getRaw().length() == 0) {
            // Null article, null reference, zero-length reference: skip them all
            return null;
        }

        ArrayList<Pair<Article, Pair<Article, Float>>> candidates = filterByTitle(article);
        candidates = filterByAuthors(candidates);

        ArrayList<Result> results = new ArrayList<>(candidates.size());
        for (int i = 0; i < candidates.size(); ++i) {
            Result result = new Result();

            result.id = candidates.get(i).getKey().getId();
            result.results.put(candidates.get(i).getValue().getKey().getId(), candidates.get(i).getValue().getValue());

            results.add(result);
        }

        return results;
    }

    /**
     * ES stores all the titles
     * This function queries ES for titles that may be contained the input article's reference
     * Boolean model makes ES search results fluctuate too much
     * Therefore another check must be applied
     * Bitap is used to compare the result title to the references
     *
     * @param article The article whose reference will be searched for
     * @return filteredCandidates The possible articles, along with theirs title scores
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private static ArrayList<Pair<Article, Pair<Article, Float>>> filterByTitle(Article article) throws UnsupportedEncodingException, URISyntaxException {
        ArrayList<Article> candidates = CitationSearch.getArticles(Config.ES_TITLE_FIELD_NAME, article.getReferences().getRaw());
        ArrayList<Pair<Article, Pair<Article, Float>>> filteredCandidates = new ArrayList<>();

        for (Article candidate : candidates) {
            float[] bitapResult = Bitap.match(article.getReferences().getRaw(), candidate.getTitle(), 0.1f);
            if (bitapResult[0] == -1) {
                // Not found, just skip
                continue;
            }

            // Found, now drill down to find the exact reference line
            String[] references = article.getReferences().getSeparatedReferences();
            for (String reference : references) {
                bitapResult = Bitap.match(reference, candidate.getTitle(), 0.1f);

                if (bitapResult[0] != -1) {
                    // Found exact reference line
                    Article temp = new Article();

                    temp.setId(article.getId());
                    temp.setReferences(new References(reference));

                    filteredCandidates.add(new Pair<>(candidate, new Pair<>(temp, bitapResult[1])));
                }
            }
        }

        return filteredCandidates;
    }

    private static ArrayList<Pair<Article, Pair<Article, Float>>> filterByAuthors(ArrayList<Pair<Article, Pair<Article, Float>>> candidates) {
        for (int i = 0; i < candidates.size(); ++i) {

            ArrayList<ArrayList<String>> authors = candidates.get(i).getKey().getPossibleAuthorNames();
            float titleScore = candidates.get(i).getValue().getValue();

            String reference = candidates.get(i).getValue().getKey().getReferences().getRaw().toLowerCase();
            int matchedAuthors = 0;

            for (ArrayList<String> author : authors) {
                for (String possibleName : author) {
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
            } else {
                // Update score
                candidates.set(i, new Pair<>(candidates.get(i).getKey(), new Pair<>(candidates.get(i).getValue().getKey(), overallScore)));
            }
        }

        return candidates;
    }
}
