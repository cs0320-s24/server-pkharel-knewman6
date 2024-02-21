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

public class TestView {
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

    private static HttpURLConnection tryRequest(String apiCall) throws IOException {
        URL requestURL;
        if (apiCall.equals("viewcsv")) {
            requestURL = new URL("http://localhost:3231/" + apiCall);
        } else {
            requestURL = new URL("http://localhost:3231/" + apiCall + "?filepath=src/main/java/edu/brown/cs/student/main/data/file.csv");
        }
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
        clientConnection.setRequestMethod("GET");
        clientConnection.connect();
        return clientConnection;
    }

    @Test
    public void testValidView() throws IOException {
        try {
            // Add a small delay to give the server more time to initialize
            Thread.sleep(1000); // 1000 milliseconds = 1 second
            // Rest of your test code...
        this.tryRequest("loadcsv");
        HttpURLConnection connection = tryRequest("viewcsv");
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        String response = connection.getResponseMessage();
        assertTrue(response.contains("success"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUnloadedView() throws IOException {
        try {
            // Add a small delay to give the server more time to initialize
            Thread.sleep(1000); // 1000 milliseconds = 1 second
            // Rest of your test code...
        HttpURLConnection connection = tryRequest("viewcsv");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());
        String response = connection.getResponseMessage();
        assertTrue(response.contains("No CSV file is currently loaded"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}