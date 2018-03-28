package vn.edu.vnu.uet.data;

import java.util.HashMap;

public class Result {
    public int id;
    public HashMap<Integer, Float> results;

    public Result() {
        results = new HashMap<>();
    }

    public Result(int id, HashMap<Integer, Float> results) {
        this.id = id;
        this.results = results;
    }
}
