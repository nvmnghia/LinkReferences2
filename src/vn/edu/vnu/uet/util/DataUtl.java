package vn.edu.vnu.uet.util;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import vn.edu.vnu.uet.config.Config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;

public class DataUtl {

    public static ResultSet queryDB(String dbName, String query) throws SQLException {
        // Connect to the DB
        Connection connection = null;
//        try {
//            Class.forName("com.mysql.jdbc.Driver").newInstance();
//            connection = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=");
//        } catch (Exception ex) {
//            // whoohoo what a broken implementation
//        }
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Statement statement = connection.createStatement();
        statement.executeQuery("USE " + dbName);

        // Query
        ResultSet resultSet = statement.executeQuery(query);
        return null;
    }

    private static Client client = null;

    public static SearchHits queryES(String index, QueryBuilder builder) throws UnknownHostException {
        if (client == null) {
            TransportAddress address = new TransportAddress(InetAddress.getByName("localhost"), 9300);
            Settings settings = Settings.builder()
                    .put("cluster.name", Config.ES_CLUSTER_NAME)
                    .build();

            client = new PreBuiltTransportClient(settings).addTransportAddress(address);
        }

        return client.prepareSearch(index).setQuery(builder).get().getHits();
    }
}
