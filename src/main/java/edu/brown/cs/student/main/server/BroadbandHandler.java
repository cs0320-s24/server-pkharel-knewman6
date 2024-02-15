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
            String broadbandJson = this.sendRequest(state, county);
            responseMap.put("result", "success");
            responseMap.put("data", broadbandJson);
            response.status(200);
        } catch (Exception e) {
            responseMap.put("result", "error");
            responseMap.put("message", e.getMessage());
            response.status(500);
        }

        return responseMap;
    }

    private String sendRequest(String state, String county) throws URISyntaxException, IOException, InterruptedException {
        String baseUri = "https://api.census.gov/data/2010/dec/sf1";
        String queryParam = "?get=NAME&for=county:" + county + "&in=state:" + state;

        URI uri = new URI(baseUri + queryParam);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        // Return the body of the response
        return response.body();
    }
}
