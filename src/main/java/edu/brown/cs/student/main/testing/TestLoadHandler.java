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
        Spark.stop();
        Spark.port(0);
        Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
    }

    @BeforeEach
    public void setup() {
        Spark.get("loadcsv", new LoadCSVHandler());
        Spark.init();
        Spark.awaitInitialization();
    }

    @AfterEach
    public void teardown() {
        Spark.unmap("loadcsv");
        Spark.awaitStop();
    }

    private static HttpURLConnection tryRequest(String apiCall, String filepath) throws IOException {
        URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall+ "?filepath=" + filepath);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
        clientConnection.setRequestMethod("GET");

        clientConnection.connect();
        return clientConnection;
    }

    @Test
    public void testLoadCSVWithValidPath() throws IOException {
        try {
            // Add a small delay to give the server more time to initialize
            Thread.sleep(1000); // 1000 milliseconds = 1 second
            String validFilePath = "src/main/java/edu/brown/cs/student/main/data/file.csv";
            HttpURLConnection connection = tryRequest("loadcsv", validFilePath);
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            String responseBody = connection.getResponseMessage();
            assertTrue(responseBody.contains("\"result\":\"success\""));
            assertTrue(responseBody.contains("\"message\":\"CSV file loaded successfully\""));
            assertTrue(responseBody.contains((validFilePath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadCSVWithInvalidPath() throws IOException {
        try {
            // Add a small delay to give the server more time to initialize
            Thread.sleep(1000); // 1000 milliseconds = 1 second
            // Rest of your test code...
        String invalidFilePath = "invalid/path/to/csv.csv";
        HttpURLConnection connection = tryRequest("loadcsv", invalidFilePath);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());

        String responseBody = connection.getResponseMessage();
        assertTrue(responseBody.contains("\"result\":\"error\""));
        assertTrue(responseBody.contains("File does not exist or is a directory"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadCSVWithNoPath() throws IOException {
        try {
            // Add a small delay to give the server more time to initialize
            Thread.sleep(1000); // 1000 milliseconds = 1 second
            // Rest of your test code...
        HttpURLConnection connection = tryRequest("loadcsv", "");

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());

        String responseBody = connection.getResponseMessage();
        assertTrue(responseBody.contains("\"result\":\"error\""));
        assertTrue(responseBody.contains("No file path provided"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadCSVOutsideAllowedDirectory() throws IOException {
        try {
            // Add a small delay to give the server more time to initialize
            Thread.sleep(1000); // 1000 milliseconds = 1 second
            // Rest of your test code...
        String filepath = "/Users/konewman/Desktop/CS0320/csv-konewman121/data/empty_csv.csv";
        HttpURLConnection connection = tryRequest("loadcsv", filepath);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, connection.getResponseCode());
        String responseBody = connection.getResponseMessage();
        String expectedErrorMessage = "File is not within the allowed directory";
        assertTrue(responseBody.contains(expectedErrorMessage), "Response should contain an error message about the file being outside the allowed directory");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
