package edu.brown.cs.student.main.testing;

import edu.brown.cs.student.main.server.Endpoints.LoadCSVHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import spark.Spark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;

public class TestLoadHandler {

    @BeforeClass
    public static void setup_before_everything() {
        Spark.port(3231);
        Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
    }

    @BeforeEach
    public void setup() {

        // In fact, restart the entire Spark server for every test!
        Spark.get("loadcsv", new LoadCSVHandler());
        Spark.init();
        Spark.awaitInitialization(); // don't continue until the server is listening
    }

    @AfterEach
    public void teardown() {
        // Gracefully stop Spark listening on both endpoints after each test
        Spark.unmap("loadcsv");
        Spark.awaitStop(); // don't proceed until the server is stopped
    }

    private static HttpURLConnection tryRequest(String apiCall, String filepath) throws IOException {
        URL requestURL = new URL("http://localhost:3231/" + apiCall + "?filepath=" + filepath);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
        clientConnection.setRequestMethod("GET");

        clientConnection.connect();
        return clientConnection;
    }

    @Test
    public void testLoadCSVWithValidPath() throws IOException {
        String validFilePath = "src/main/java/edu/brown/cs/student/main/data/file.csv";
        HttpURLConnection connection = tryRequest("loadcsv", validFilePath);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        String responseBody = connection.getResponseMessage();
        assertTrue(responseBody.contains("\"result\":\"success\""));
        assertTrue(responseBody.contains("\"message\":\"CSV file loaded successfully\""));
        assertTrue(responseBody.contains((validFilePath)));
    }

    @Test
    public void testLoadCSVWithInvalidPath() throws IOException {
        String invalidFilePath = "invalid/path/to/csv.csv";
        HttpURLConnection connection = tryRequest("loadcsv", invalidFilePath);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());

        String responseBody = connection.getResponseMessage();
        assertTrue(responseBody.contains("\"result\":\"error\""));
        assertTrue(responseBody.contains("File does not exist or is a directory"));
    }

    @Test
    public void testLoadCSVWithNoPath() throws IOException {
        HttpURLConnection connection = tryRequest("loadcsv", "");

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());

        String responseBody = connection.getResponseMessage();
        assertTrue(responseBody.contains("\"result\":\"error\""));
        assertTrue(responseBody.contains("No file path provided"));
    }

    @Test
    public void testLoadCSVOutsideAllowedDirectory() throws IOException {
        String filepath = "/Users/konewman/Desktop/CS0320/csv-konewman121/data/empty_csv.csv";
        HttpURLConnection connection = tryRequest("loadcsv", filepath);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());
        String responseBody = connection.getResponseMessage();
        String expectedErrorMessage = "File is not within the allowed directory";
        assertTrue(responseBody.contains(expectedErrorMessage), "Response should contain an error message about the file being outside the allowed directory");
    }


}
