package edu.brown.cs.student.main.testing;
import edu.brown.cs.student.main.server.CSVHolder;
import edu.brown.cs.student.main.server.Endpoints.LoadCSVHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testng.annotations.*;
import org.testng.annotations.Test;
import spark.Spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.Assert.*;

public class TestSearch {
    @BeforeClass
    public static void setup_before_everything() {
        Spark.port(3231);
        Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
    }

    @BeforeEach
    public void setup() {

        // In fact, restart the entire Spark server for every test!
        Spark.get("searchcsv", new LoadCSVHandler());
        Spark.get("loadcsv", new LoadCSVHandler());
        Spark.init();
        Spark.awaitInitialization(); // don't continue until the server is listening
    }

    @AfterEach
    public void teardown() {
        // Gracefully stop Spark listening on both endpoints after each test
        CSVHolder.getInstance().unloadCSV();
        Spark.unmap("searchcsv");
        Spark.awaitStop(); // don't proceed until the server is stopped
    }

    private static HttpURLConnection tryRequest(String apiCall, String query, String column, String headers) throws IOException {
        URL requestURL;
        if(apiCall.equals("searchcsv")) {
            requestURL = new URL("http://localhost:3231/" + apiCall + "?query=" + query +
                    "&column=" + column + "&headers=" + headers);
        }
        else{
            requestURL = new URL("http://localhost:3231/" + apiCall + "?filepath=src/main/java/edu/brown/cs/student/main/data/file.csv");
        }
        HttpURLConnection clientConnection = (HttpURLConnection)requestURL.openConnection();
        clientConnection.setRequestMethod("GET");
        clientConnection.connect();
        return clientConnection;
    }

    @Test
    public void testSearchWithoutQuery() throws IOException {
        this.tryRequest("loadcsv", "valid/path/to/csv", null, null);
        HttpURLConnection connection = tryRequest("searchcsv", null, "columnName", "true");

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());
        String response = connection.getResponseMessage();
        assertTrue(response.contains("Query and column parameters are required."), "Expected error message about missing query parameter");
    }


    @Test
    public void testSearchWithValidParameters() throws IOException {
        this.tryRequest("loadcsv", null, null, null);
        HttpURLConnection connection = tryRequest("searchcsv", "searchQuery", "columnName", "true");

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
    }

    @Test
    public void testSearchWithoutColumn() throws IOException {
        this.tryRequest("loadcsv", "valid/path/to/csv", null, null);
        HttpURLConnection connection = tryRequest("searchcsv", "searchQuery", null, "true");
        String response = connection.getResponseMessage();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());
        assertTrue(response.contains("Query and column parameters are required."), "Expected error message about missing column parameter");
    }

    @Test
    public void testSearchWithCSVNotLoaded() throws IOException {
        this.tryRequest("loadcsv", null, null, null); // Simulating no CSV loaded
        HttpURLConnection connection = tryRequest("searchcsv", "searchQuery", "columnName", "true");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());
        String response = connection.getResponseMessage();
        assertTrue(response.contains("No CSV file is currently loaded!"), "Expected error message about no CSV file being loaded");
    }


}
