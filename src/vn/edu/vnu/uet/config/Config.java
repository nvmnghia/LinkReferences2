package vn.edu.vnu.uet.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import sun.net.www.http.HttpClient;
import vn.edu.vnu.uet.data.Article;
import vn.edu.vnu.uet.data.References;
import vn.edu.vnu.uet.util.DataUtl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class Config {
    // ES
    public static String ES_CLUSTER_NAME = "docker-cluster";
    public static String ES_INDEX = "test";
    public static String ES_TYPE = "article";

    public static String ES_URL = "http://vcgate.vnu.edu.vn";
    public static int ES_PORT = 29200;

    public static String ES_SEARCH_URI = Config.ES_URL + ":" + Config.ES_PORT + "/" + Config.ES_INDEX + "/" + Config.ES_TYPE + "/_search?q=";

    public static String ES_ID_FIELD_NAME = "id";
    public static String ES_TITLE_FIELD_NAME = "title";
    public static String ES_REFERENCES_FIELD_NAME = "reference";
    public static String ES_AUTHORS_FIELD_NAME = "articles_authors_data";

//    // ES
//    public static String ES_CLUSTER_NAME = "vci-scholar";
//    public static String ES_INDEX = "articles";
//    public static String ES_TYPE = "article";
//
//    public static String ES_ADDRESS = "localhost";
//    public static int ES_PORT = 9300;

    // DB
    public static String DB_NAME = "vci_scholar";
}
