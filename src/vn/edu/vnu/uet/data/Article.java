package vn.edu.vnu.uet.data;

import java.util.ArrayList;

public class Article {
    private int id;
    private String title;
    private References references;
    private String DOI;
    private String[] authors;

    public Article() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public References getReferences() {
        return references;
    }

    public void setReferences(References references) {
        this.references = references;
    }

    public String getDOI() {
        return DOI;
    }

    public void setDOI(String DOI) {
        this.DOI = DOI;
    }

    public String[] getAuthors() {
        return authors;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    /**
     * This function returns a list of all author names, including deviant of citation style.
     * @return list of all possible authors' names
     */
    public ArrayList<ArrayList<String>> getPossibleAuthorNames() {
        if (authors == null) {
            return new ArrayList<>();
        }

        ArrayList<ArrayList<String>> possibleAuthorName = new ArrayList<>();

        for (String author : authors) {
            ArrayList<String> possibleNames = new ArrayList<>();
            possibleNames.add(author.toLowerCase());

            ArrayList<String> guessedNames = Name.generateAbbrNames(author);
            if (guessedNames != null) {
                possibleNames.addAll(guessedNames);
            }

            possibleAuthorName.add(possibleNames);
        }

        return possibleAuthorName;
    }
}
