package vn.edu.vnu.uet.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import vn.edu.vnu.uet.config.Config;
import vn.edu.vnu.uet.data.Article;
import vn.edu.vnu.uet.data.References;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

public class DataUtl {

    private static JsonParser parser = new JsonParser();
    private static Random random = new Random();

    public static JsonArray queryES(String field, String value) throws URISyntaxException, UnsupportedEncodingException {
        field = URLEncoder.encode(field, "UTF-8");
        value = URLEncoder.encode(value, "UTF-8");

        HttpPost query = new HttpPost();
        query.setURI(new URI( Config.ES_SEARCH_URI + field + ":" + value));

        try (
                CloseableHttpClient client = HttpClientBuilder.create().build();
                CloseableHttpResponse response = client.execute(query);
            ) {

            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            JsonObject rootObject = parser.parse(result).getAsJsonObject();

            // Make sure the poor server won't be banged to death
            Thread.sleep(random.nextInt(55));

            return rootObject.getAsJsonObject("hits").getAsJsonArray("hits");

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Count number of document in elastic search
     * Largest ID should be close to this number
     *
     * @return number of document in the specified Type
     * @throws URISyntaxException
     */
    public static int numOfDocument() throws URISyntaxException {
        HttpPost query = new HttpPost();
        query.setURI(new URI( Config.ES_URL + ":" + Config.ES_PORT + "/" + Config.ES_INDEX + "/" + Config.ES_TYPE + "/_count"));

        try (
                CloseableHttpClient client = HttpClientBuilder.create().build();
                CloseableHttpResponse response = client.execute(query);
        ) {

            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            JsonObject rootObject = parser.parse(result).getAsJsonObject();

            return rootObject.getAsJsonPrimitive("count").getAsInt();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
