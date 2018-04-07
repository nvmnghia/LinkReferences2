package vn.edu.vnu.uet.es;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import vn.edu.vnu.uet.data.Result;
import vn.edu.vnu.uet.util.DataUtl;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Client {
    public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrayList<Result> results = new ArrayList<>();

        for (String id : args) {
            results.addAll(CitationSearch.getResults(id));
        }

        System.out.println(gson.toJson(results));
    }
}
