package vn.edu.vnu.uet.es;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.util.Pair;

import vn.edu.vnu.uet.bitap.Bitap;
import vn.edu.vnu.uet.config.Config;
import vn.edu.vnu.uet.data.Article;
import vn.edu.vnu.uet.data.References;
import vn.edu.vnu.uet.data.Result;
import vn.edu.vnu.uet.util.DataUtl;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Given an article ID, search for documents that cite it (i.e. in which it is Cited)
 */

public class CitedSearch {


    /**
     * Get the search result, which includes articles IDS and theirs scores
     *
     * @param articleID
     * @return Result: Article IDs and theirs scores
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    protected static Result getResult(String articleID) throws UnsupportedEncodingException, URISyntaxException {
        Article article = CitationSearch.getArticleByID(articleID);
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
     * ES stores all the references
     * This function queries ES for references that may contain the input article's title
     * Boolean model makes ES search results fluctuate too much
     * Therefore another check must be applied
     * Bitap is used to compare the result title to the references
     *
     * @param article The article whose title will be searched for
     * @return filteredCandidates The possible articles, along with theirs title scores
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private static ArrayList<Pair<Article, Float>> filterByTitle(Article article) throws UnsupportedEncodingException, URISyntaxException {
        ArrayList<Article> candidates = CitationSearch.getArticles(Config.ES_REFERENCES_FIELD_NAME, article.getTitle());
        ArrayList<Pair<Article, Float>> filteredCandidates = new ArrayList<>();

        for (Article candidate : candidates) {
            // Skip articles which has meaningless reference
            if (candidate.getReferences().getRaw() == null) {
                continue;
            }

            float[] bitapResult = Bitap.match(candidate.getReferences().getRaw(), article.getTitle(), 0.1f);
            if (bitapResult[0] == -1) {
                // Not found, just skip
                continue;
            }

            // Found, now drill down to find the exact reference line
            String[] references = candidate.getReferences().getSeparatedReferences();
            for (String reference : references) {
                bitapResult = Bitap.match(reference, article.getTitle(), 0.1f);

                if (bitapResult[0] != -1) {
                    // Found exact reference line
                    Article filteredCandidate = new Article();

                    // Only author information is needed
                    filteredCandidate.setId(candidate.getId());
                    filteredCandidate.setReferences(new References(reference));

                    filteredCandidates.add(new Pair<>(filteredCandidate, bitapResult[1]));
                }
            }
        }

        return filteredCandidates;
    }

    /**
     * Use author information to refine results
     * Articles are removed if the overall accuracy is lower than 0.5f
     *
     * @param article The article whose author will be searched for
     * @param candidates The possible articles
     * @return candidates The possible articles, along with theirs overall scores
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
            } else {
                // Update score
                candidates.set(i, new Pair<>(candidates.get(i).getKey(), overallScore));
            }
        }

        return candidates;
    }
}
