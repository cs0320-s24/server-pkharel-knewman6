package edu.brown.cs.student.main.testing;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.Endpoints.broadband.BroadbandHandler;
import edu.brown.cs.student.main.server.Endpoints.broadband.IBroadbandHelper;
import edu.brown.cs.student.main.server.Endpoints.broadband.MockBroadbandHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import spark.Spark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.*;

public class TestBroadband {
    private static IBroadbandHelper mocker = new MockBroadbandHelper();
    private BroadbandHandler handler = new BroadbandHandler(mocker);

    @BeforeClass
    public static void setup_before_everything() {
        Spark.stop();
        Spark.port(0);
        Logger.getLogger("").setLevel(Level.WARNING);
    }

    @BeforeEach
    public void setup() {
        Spark.get("broadband", handler);
        Spark.init();
        Spark.awaitInitialization();
    }

    @AfterEach
    public void teardown() {
        Spark.unmap("broadband");
        Spark.awaitStop();
    }

    private static HttpURLConnection tryRequest(String apiCall, String county, String state) throws IOException {
        String stateCode = mocker.getStateCode(state);
        URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall+ "?get=NAME,S2802_C03_022E&for=county:" + county + "&in=state:" + stateCode);
        HttpURLConnection clientConnection = (HttpURLConnection)requestURL.openConnection();
        clientConnection.setRequestMethod("GET");
        clientConnection.connect();
        return clientConnection;
    }

    @Test
    public void testValidRequest() throws IllegalStateException {
        try {
            // Add a small delay to give the server more time to initialize
            Thread.sleep(1000);
            HttpURLConnection connection = tryRequest("broadband", "mockCounty", "mockState");
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        } catch (Exception e) {
            System.out.print(e.getMessage());
            assertEquals(1,0); //force fail to show exception
        }
    }

    @Test
    public void testMissingStateParameter() {
        try {
            Thread.sleep(1000);
            assertThrows(IllegalArgumentException.class, () -> {
                Thread.sleep(1000);
                tryRequest("broadband", "validCounty", "");
            });
        } catch (Exception e) {
            System.out.print(e.getMessage());
            assertEquals(1,0);
        }
    }

    @Test
    public void testMissingCountyParameter() {
        try {
            Thread.sleep(1000);
            assertThrows(IllegalStateException.class, () -> {
                Thread.sleep(1000);
                tryRequest("broadband", "", "mockState");
            });
        } catch (Exception e) {
            System.out.print(e.getMessage());
            assertEquals(1,0);
        }
    }

    @Test
    public void testBlankCountyParameter() {
        try {
            Thread.sleep(1000);
            assertThrows(IllegalStateException.class, () -> {
                Thread.sleep(1000);
                tryRequest("broadband", " ", "mockState");
            });
        } catch (Exception e) {
            System.out.print(e.getMessage());
            assertEquals(1,0);
        }
    }

    @Test
    public void testBlankStateParameter() {
        try {
            Thread.sleep(1000);
            assertThrows(IllegalArgumentException.class, () -> {
                Thread.sleep(1000);
                tryRequest("broadband", "mockCounty", " ");
            });
        } catch (Exception e) {
            System.out.print(e.getMessage());
            assertEquals(1,0);
        }
    }

    @Test
    public void testInvalidStateParameter() {
        try {
            Thread.sleep(1000);
            assertThrows(IllegalArgumentException.class, () -> {
                Thread.sleep(1000);
                tryRequest("broadband", "mockCounty", "no state");
            });
        } catch (Exception e) {
            System.out.print(e.getMessage());
            assertEquals(1,0);
        }
    }

    @Test
    public void testInvalidCountyParameter() {
        try {
            Thread.sleep(1000);
            assertThrows(IllegalArgumentException.class, () -> {
                Thread.sleep(1000);
                tryRequest("broadband", "no county", "mockState");
            });
        } catch (Exception e) {
            System.out.print(e.getMessage());
            assertEquals(1,0);
        }
    }

    @Test
    public void testSuccessfulRequestWithValidParameters() {
        try {
            Thread.sleep(1000);
            HttpURLConnection connection = tryRequest("broadband", "mockCounty", "mockState");
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();
            Moshi moshi = new Moshi.Builder().build();
            Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);
            Map<String, Object> responseMap = adapter.fromJson(responseBuilder.toString());
            assertEquals("success", responseMap.get("result"));
            assertTrue(responseMap.containsKey("data"));
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(1, 0);
        }
    }


}
