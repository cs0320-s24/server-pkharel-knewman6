package edu.brown.cs.student.main.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        Map<String, Object> responseMap = new HashMap<>();

        String stateName = request.queryParams("state");
        String county = request.queryParams("county");

        if (stateName == null || county == null || stateName.isEmpty() || county.isEmpty()) {
            responseMap.put("result", "error");
            responseMap.put("message", "State and county parameters are required.");
            return responseMap;
        }

        try {
            String stateCode = getStateCode(stateName);
            String broadbandJson = this.sendRequest(stateCode, county);
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

    private String getStateCode(String stateName) throws IOException, InterruptedException, URISyntaxException, IllegalArgumentException {
        String stateDirectoryUri = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*";
        HttpRequest stateRequest = HttpRequest.newBuilder()
                .uri(new URI(stateDirectoryUri))
                .GET()
                .build();
        HttpResponse<String> stateResponse = httpClient.send(stateRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = stateResponse.body();
        Pattern pattern = Pattern.compile("\\[\"(.*?)\",\"(\\d{2})\"\\]");
        Matcher matcher = pattern.matcher(responseBody);

        while (matcher.find()) {
            if (matcher.group(1).equalsIgnoreCase(stateName)) {
                return matcher.group(2); // Return the state code
            }
        }
        throw new IllegalArgumentException("State name not found: " + stateName);
    }

    private String sendRequest(String stateCode, String county) throws URISyntaxException, IOException, InterruptedException {
        String baseUri = "https://api.census.gov/data/2021/acs/acs1/subject/variables";
        String queryParam = "?get=NAME,S2802_C03_022E&for=county:" + county + "&in=state:" + stateCode;
        //https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:*&in=state:06
        URI uri = new URI(baseUri + queryParam);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Return the body of the response
        return response.body();
    }
}
