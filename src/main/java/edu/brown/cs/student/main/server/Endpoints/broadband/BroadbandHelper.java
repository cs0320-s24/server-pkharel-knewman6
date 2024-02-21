package edu.brown.cs.student.main.server.Endpoints.broadband;

import edu.brown.cs.student.main.server.Caching.BroadbandCache;

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


/**
 * Provides utility methods to support broadband data fetching operations.
 * This class includes methods for initializing state codes from an external API,
 * retrieving state and county codes based on names, and fetching broadband data
 * for a specific state and county.
 */
public class BroadbandHelper implements IBroadbandHelper{
    private Map<String, String> stateCodeMap = new HashMap<>();


    /**
     * Initializes a map with state names and their corresponding codes by fetching
     * data from the external ACS API. This method parses the response and populates
     * the provided map with state name-code pairs.
     *
     * @throws URISyntaxException if the URI for the API request is incorrect.
     * @throws IOException if an I/O exception occurs during the API request.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void initializeStateCodes()
            throws URISyntaxException, IOException, InterruptedException {
        String stateDirectoryUri = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*";
        HttpRequest stateRequest =
                HttpRequest.newBuilder().uri(new URI(stateDirectoryUri)).GET().build();
        HttpResponse<String> stateResponse =
                HttpClient.newHttpClient().send(stateRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = stateResponse.body();
        String[] lines = responseBody.split("\n", 2);
        String statesData = lines.length > 1 ? lines[1] : "";
        Pattern pattern = Pattern.compile("\\[\"(.*?)\",\"(\\d{2})\"\\]");
        Matcher matcher = pattern.matcher(statesData);

        while (matcher.find()) {
            String stateName = matcher.group(1).replace("\"", "");
            String stateCode = matcher.group(2).replace("\"", "");
            this.stateCodeMap.put(stateName, stateCode);
        }
    }


    /**
     * Retrieves the code for a given state name from a map of state name-code pairs.
     *
     * @param stateName the name of the state.
     * @return The code of the state.
     * @throws IllegalArgumentException if the state name is not found in the map.
     */
    public String getStateCode(String stateName)
            throws IllegalArgumentException {
        if (stateName.equals("*")) {
            return "*";
        }
        String stateCode = this.stateCodeMap.get(stateName);
        if (stateCode == null) {
            throw new IllegalArgumentException("State name not found: " + stateName);
        }
        return stateCode;
    }

    /**
     * Retrieves the code for a given county name within a state by making an API request.
     * This method matches the county name against the API response to find the corresponding code.
     *
     * @param stateCode the state code where the county is located.
     * @param countyName the name of the county.
     * @param client the HttpClient used to make the API request.
     * @return The code of the county.
     * @throws IOException if an I/O exception occurs during the API request.
     * @throws InterruptedException if the operation is interrupted.
     * @throws URISyntaxException if the URI for the API request is incorrect.
     * @throws IllegalArgumentException if the county name is not found.
     */
    public String getCountyCode(String stateCode, String countyName, HttpClient client)
            throws IOException, InterruptedException, URISyntaxException, IllegalArgumentException {
        if (countyName.equals("*")) {
            return "*";
        }
        String countyDirectoryUri =
                "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode;
        HttpRequest countyRequest =
                HttpRequest.newBuilder().uri(new URI(countyDirectoryUri)).GET().build();
        HttpResponse<String> countyResponse =
                client.send(countyRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = countyResponse.body();
        Pattern pattern =
                Pattern.compile("\\[\"(" + countyName + ", .*?)\",\"" + stateCode + "\",\"(\\d{3})\"\\]");
        Matcher matcher = pattern.matcher(responseBody);

        while (matcher.find()) {
            String matchedCounty = matcher.group(1).split(",")[0].trim();
            if (matchedCounty.equalsIgnoreCase(countyName)) {
                return matcher.group(2);
            }
        }

        throw new IllegalArgumentException("County name not found: " + countyName);
    }


    /**
     * Fetches broadband data for a specific county within a state by making an API request.
     * This method constructs a request to an external API with state and county codes and
     * returns the API response. Only called if the data is not in the cache
     *
     * @param stateCode the state code.
     * @param county the county code.
     * @param client the HttpClient used to make the API request.
     * @return The API response containing broadband data.
     * @throws URISyntaxException if the URI for the API request is incorrect.
     * @throws IOException if an I/O exception occurs during the API request.
     * @throws InterruptedException if the operation is interrupted.
     */
    public String fetchDataFromApi(String stateCode, String county, HttpClient client)
            throws URISyntaxException, IOException, InterruptedException {
        String baseUri = "https://api.census.gov/data/2021/acs/acs1/subject/variables";
        String queryParam = "?get=NAME,S2802_C03_022E&for=county:" + county + "&in=state:" + stateCode;
        URI uri = new URI(baseUri + queryParam);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    @Override
    public Map<String, Object> processBroadbandRequest(String stateName, String countyName, HttpClient httpClient, BroadbandCache cacher) throws Exception {
        Map<String, Object> responseMap = new HashMap<>();

        if (stateName == null || countyName == null || stateName.isEmpty() || countyName.isEmpty()) {
            throw new IllegalArgumentException("State and county parameters are required.");
        }

        String stateCode = getStateCode(stateName);
        String countyCode = getCountyCode(stateCode, countyName, httpClient);
        String broadbandJson = cacher.getData(stateCode, countyCode);

        if (broadbandJson == null) {
            broadbandJson = fetchDataFromApi(stateCode, countyCode, httpClient);
            cacher.putData(stateCode, countyCode, broadbandJson);
        }

        responseMap.put("state", stateName);
        responseMap.put("county", countyName);
        responseMap.put("result", "success");
        responseMap.put("data", broadbandJson);

        return responseMap;
    }

}

