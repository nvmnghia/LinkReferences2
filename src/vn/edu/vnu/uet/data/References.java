package vn.edu.vnu.uet.data;

import java.util.HashMap;

public class References {
    private String raw;
    private String[] references = null;
    private HashMap<Integer, Float> articleIDs = null;

    public References() {
    }

    public References(String raw) {
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public HashMap<Integer, Float> getArticleIDs() {
        return articleIDs;
    }

    public void addArticleID(Integer id, float score) {
        if (articleIDs != null) {
            articleIDs.put(id, score);
        }
    }

    public String[] getSeparatedReferences() {
        if (references == null) {
            references = processRaw(raw);
        }

        return references;
    }

    /**
     * Split the raw references to individual references
     * Separators include:
     * - New line
     * - <br> tag
     *
     * @param raw
     * @return
     */
    public static String[] processRaw(String raw) {
        if (raw.contains("<br>")) {
            return raw.split("<br>");
        }

        String[] temp = raw.split("\\[[0-9]{1,2}]");
        if (temp.length > 1) {
            return temp;
        }

        temp = raw.split("[\r\n]+[0-9]{1,2}.");
        if (temp.length > 1) {
            return temp;
        }

        return raw.split("\n");
    }
}
