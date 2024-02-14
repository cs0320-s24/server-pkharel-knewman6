package edu.brown.cs.student.main.server;

import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BroadbandHandler implements Route {

    @Override
    public Object handle(Request request, Response response) throws IOException {
        Map<String, Object> jsonResponse = new HashMap<>();

        // Retrieve parameters from the request
        String state = request.queryParams("state");
        String county = request.queryParams("county");

        // Perform error handling for missing parameters
        if (state == null || county == null || state.isEmpty() || county.isEmpty()) {
            jsonResponse.put("result", "error_bad_request");
            jsonResponse.put("message", "State and county parameters are required.");
            return mapToJson(jsonResponse);
        }

        // Call method to fetch broadband data from the Census API
        // You will need to implement this method based on the specific query requirements

        // Populate JSON response with fetched data
        jsonResponse.put("result", "success");
        // Add any other necessary fields to the JSON response

        // Serialize response to JSON and return
        return mapToJson(jsonResponse);
    }

    // Helper method to convert map to JSON string
    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\", ");
            } else {
                json.append(entry.getValue()).append(", ");
            }
        }
        // Remove trailing comma and space
        if (json.length() > 1) {
            json.delete(json.length() - 2, json.length());
        }
        json.append("}");
        return json.toString();
    }
}
