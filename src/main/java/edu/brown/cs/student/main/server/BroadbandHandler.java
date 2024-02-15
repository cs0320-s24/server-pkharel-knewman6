package edu.brown.cs.student.main.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        Map<String, Object> responseMap = new HashMap<>();

        String state = request.queryParams("state");
        String county = request.queryParams("county");

        if (state == null || county == null || state.isEmpty() || county.isEmpty()) {
            responseMap.put("result", "error");
            responseMap.put("message", "State and county parameters are required.");
            return responseMap;
        }

        try {
            // Sends a request to the Census API and receives JSON back
            String broadbandJson = this.sendRequest(state, county);
            // Here you would typically parse the JSON into a domain object
            // For simplicity, we're directly adding the JSON response to the response map
            responseMap.put("result", "success");
            responseMap.put("data", broadbandJson);
            response.status(200);
        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("result", "error");
            responseMap.put("message", "An error occurred while processing the broadband data retrieval.");
            response.status(500);
        }

        return responseMap;
    }

    private String sendRequest(String state, String county) throws URISyntaxException, IOException, InterruptedException {
        String baseUri = "https://api.census.gov/data/2010/dec/sf1";
        // Construct the query parameter for 'get' which includes the variables you want from the API
        String queryParam = "?get=PCT012A015&for=county:" + county + "&in=state:" + state;

        // Build the full URI including the base URI and the query parameters
        URI uri = new URI(baseUri + queryParam);

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        // Send the request using HttpClient
        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        // Return the body of the response
        return response.body();
    }
}
