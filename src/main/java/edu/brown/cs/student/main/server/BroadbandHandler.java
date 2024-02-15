package edu.brown.cs.student.main.server;

import spark.Request;
import spark.Response;
import spark.Route;

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

public class BroadbandHandler implements Route {

    private static final Map<String, String> stateNameToCode = new HashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    static {
        try {
            initializeStateCodes();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
            // Handle the exceptions appropriately, perhaps logging them or setting an application error state
        }
    }

    private static void initializeStateCodes() throws URISyntaxException, IOException, InterruptedException {
        String stateDirectoryUri = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*";
        HttpRequest stateRequest = HttpRequest.newBuilder()
                .uri(new URI(stateDirectoryUri))
                .GET()
                .build();
        HttpResponse<String> stateResponse = HttpClient.newHttpClient().send(stateRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = stateResponse.body();
        String[] lines = responseBody.split("\n", 2);
        String statesData = lines.length > 1 ? lines[1] : "";
        Pattern pattern = Pattern.compile("\\[\"(.*?)\",\"(\\d{2})\"\\]");
        Matcher matcher = pattern.matcher(statesData);

        while (matcher.find()) {
            String stateName = matcher.group(1).replace("\"", "");
            String stateCode = matcher.group(2).replace("\"", "");
            stateNameToCode.put(stateName, stateCode);
        }
    }
    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        Map<String, Object> responseMap = new HashMap<>();

        String stateName = request.queryParams("state");
        String countyName = request.queryParams("county");

        if (stateName == null || countyName == null || stateName.isEmpty() || countyName.isEmpty()) {
            responseMap.put("result", "error");
            responseMap.put("message", "State and county parameters are required.");
            return responseMap;
        }

        try {
            String stateCode = getStateCode(stateName);
            String countyCode = getCountyCode(stateCode, countyName);
            String broadbandJson = BroadbandCache.getData(stateCode, countyCode); // Check cache first
            if (broadbandJson == null) {
                broadbandJson = fetchDataFromApi(stateCode, countyCode);
                BroadbandCache.putData(stateCode, countyCode, broadbandJson); // Cache the fetched data
            }
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

    private String getStateCode(String stateName) throws IllegalArgumentException {
        String stateCode = stateNameToCode.get(stateName);
        if (stateCode == null) {
            throw new IllegalArgumentException("State name not found: " + stateName);
        }
        return stateCode;
    }


    private String getCountyCode(String stateCode, String countyName) throws IOException, InterruptedException, URISyntaxException, IllegalArgumentException {
        // Construct the URI for the API call to get all counties in a state
        if(countyName.equals("*")){
            return "*";
        }
        String countyDirectoryUri = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode;
        HttpRequest countyRequest = HttpRequest.newBuilder()
                .uri(new URI(countyDirectoryUri))
                .GET()
                .build();

        // Send the request to the API
        HttpResponse<String> countyResponse = httpClient.send(countyRequest, HttpResponse.BodyHandlers.ofString());

        // The response body should contain the list of counties in the specified state
        String responseBody = countyResponse.body();

        // Pattern to match the county information in the JSON response
        // Assuming county names are unique within each state
        Pattern pattern = Pattern.compile("\\[\"(" + countyName + ", .*?)\",\"" + stateCode + "\",\"(\\d{3})\"\\]");
        Matcher matcher = pattern.matcher(responseBody);

        // Iterate over all matches and find the one that corresponds to the countyName
        while (matcher.find()) {
            // Check if the county name matches the provided countyName
            String matchedCounty = matcher.group(1).split(",")[0].trim();
            if (matchedCounty.equalsIgnoreCase(countyName)) {
                return matcher.group(2); // Return the county code
            }
        }

        throw new IllegalArgumentException("County name not found: " + countyName);
    }

    private String fetchDataFromApi(String stateCode, String county) throws URISyntaxException, IOException, InterruptedException {
        String baseUri = "https://api.census.gov/data/2021/acs/acs1/subject/variables";
        String queryParam = "?get=NAME,S2802_C03_022E&for=county:" + county + "&in=state:" + stateCode;
        URI uri = new URI(baseUri + queryParam);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("FAIL");
        return response.body();
    }
}
