package vn.edu.vnu.uet.es;

import com.google.gson.*;
import vn.edu.vnu.uet.config.Config;
import vn.edu.vnu.uet.data.Article;
import vn.edu.vnu.uet.data.References;
import vn.edu.vnu.uet.data.Result;
import vn.edu.vnu.uet.util.DataUtl;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Main search class
 * Given an article ID, returns all the articles that cite or is cited by it
 * Result is in this JSON format
 * - ID of the cited article (could be the input article, or the article which the input cites)
 * - A list of ID of articles citing the above article, along with theirs score
 * Example: A cites B, C, D. C is newly added, citing E, F, G. Only B, D, F exist in the DB.
 *     So these ID will show up in the result:
 *     [ { C : {F} }, { A: {C} } ]
 *     Get it now?
 * Score is avg of title score and author score, representing the similarity in these criteria
 * To use it, just call getResults
 */

public class CitationSearch {

    /**
     * Main search function
     *
     * @param articleID
     * @return all the articles that cite or is cited by it
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public static ArrayList<Result> getResults(String articleID) throws UnsupportedEncodingException, URISyntaxException {

        ArrayList<Result> citation = new ArrayList<>();

        Result cited = CitedSearch.getResult(articleID);
        if (cited != null) {
            citation.add(CitedSearch.getResult(articleID));
        }
        ArrayList<Result> citing = CitingSearch.getResults(articleID);
        if (citing != null) {
            citation.addAll(citing);
        }

        return citation;
    }

    /**
     * Get article from ES by its ID
     *
     * @param id id of the article
     * @return Article
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    public static Article getArticleByID(String id) throws UnsupportedEncodingException, URISyntaxException {
        ArrayList<Article> articles = getArticles(Config.ES_ID_FIELD_NAME, id);

        return articles.size() == 0 ? null : articles.get(0);
    }

    /**
     * Search articles in ES, given a field and its value
     *
     * @param field Should be one of the declared field in Config
     * @param value value to be searched for
     * @return List of matched articles
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public static ArrayList<Article> getArticles(String field, String value) throws UnsupportedEncodingException, URISyntaxException {
        JsonArray hits = DataUtl.queryES(field, value);
        if (hits == null || hits.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Article> articles = new ArrayList<>();
        for (int i = 0; i < hits.size(); ++i) {
            Article article = new Article();
            JsonObject articleObject = hits.get(i).getAsJsonObject().get("_source").getAsJsonObject();

            article.setId(articleObject.get(Config.ES_ID_FIELD_NAME).getAsInt());
            article.setTitle(articleObject.get(Config.ES_TITLE_FIELD_NAME).getAsString());

            if (articleObject.get(Config.ES_REFERENCES_FIELD_NAME) == null ||
                    articleObject.get(Config.ES_REFERENCES_FIELD_NAME) instanceof JsonNull) {
                article.setReferences(new References(null));
            } else {
                article.setReferences(new References(articleObject.get(Config.ES_REFERENCES_FIELD_NAME).getAsString()));
            }

            if (articleObject.get(Config.ES_AUTHORS_FIELD_NAME) == null ||
                    articleObject.get(Config.ES_AUTHORS_FIELD_NAME) instanceof JsonNull) {
                article.setAuthors(null);
            } else {
                article.setAuthors(getAuthors(articleObject.get(Config.ES_AUTHORS_FIELD_NAME).getAsJsonArray()));
            }

            articles.add(article);
        }

        return articles;
    }

    /**
     * Get author from the corresponding JsonObject
     * @param authorsObject containing author information
     * @return A list of author
     */
    private static String[] getAuthors(JsonArray authorsObject) {
        if (authorsObject.size() == 0) {
            return new String[0];
        }

        String[] authors = new String[authorsObject.size()];

        for (int i = 0; i < authorsObject.size(); ++i) {
            authors[i] = authorsObject.get(i).getAsJsonObject().get("name").getAsString();
        }

        return authors;
    }
}
