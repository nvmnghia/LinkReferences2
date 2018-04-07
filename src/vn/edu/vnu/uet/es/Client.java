package vn.edu.vnu.uet.es;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

public class Client {
    public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(CitationSearch.getResults("770")));
    }
}
