package edu.brown.cs.student.main.server.Endpoints.broadband;
import edu.brown.cs.student.main.server.Caching.BroadbandCache;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;


/**
 * This class implements a mock helper for broadband data fetching, intended for testing purposes.
 * It simulates the behavior of fetching state and county codes, and broadband data from an API
 * without making actual HTTP requests, to enable easier testing on the BroadbandHandler class
 */
public class MockBroadbandHelper implements IBroadbandHelper {
    private Map<String, String> mockStateCodeMap = new HashMap<>();
    private Map<String, String> mockCountyCodeMap = new HashMap<>();
    private final String mockBroadbandData = "{\"data\": \"Mock broadband data\"}";

    /**
     * Constructs a MockBroadbandHelper and initializes mock data for testing.
     */
    public MockBroadbandHelper(){
        initializeStateCodes();
    }

    /**
     * Initializes mock state and county codes for testing purposes.
     */
    private void initializeMockData() {
        mockStateCodeMap.put("mockState", "01");
        mockCountyCodeMap.put("mockCounty", "001");
    }

    /**
     * Initializes state codes. This method wraps the private method to initialize mock data.
     */
    @Override
    public void initializeStateCodes() {
        this.initializeMockData();
    }

    /**
     * Retrieves a mock state code based on a given state name.
     *
     * @param stateName the name of the state
     * @return the mock state code
     * @throws IllegalArgumentException if the state name is not found
     */
    @Override
    public String getStateCode(String stateName) throws IllegalArgumentException {
        String stateCode = mockStateCodeMap.get(stateName);
        if (stateCode == null) {
            throw new IllegalArgumentException("State name not found: " + stateName);
        }
        return stateCode;
    }

    /**
     * Retrieves a mock county code based on a given county name.
     *
     * @param stateCode the state code
     * @param countyName the name of the county
     * @param client the HTTP client (not used in the mock implementation)
     * @return the mock county code
     * @throws IllegalArgumentException if the county name is not found
     */
    @Override
    public String getCountyCode(String stateCode, String countyName, HttpClient client) throws IllegalArgumentException {
        String countyCode = mockCountyCodeMap.get(countyName);
        if (countyCode == null) {
            throw new IllegalArgumentException("County name not found: " + countyName);
        }
        return countyCode;
    }

    /**
     * Fetches mock broadband data from the "API", returning constant value
     *
     * @param stateCode the state code
     * @param county the county code
     * @param client the HTTP client (not used in this mock implementation)
     * @return a mock JSON string containing broadband data
     */
    @Override
    public String fetchDataFromApi(String stateCode, String county, HttpClient client) {
        return mockBroadbandData;
    }


    /**
     * Processes a broadband data request using mock data.
     *
     * @param stateName the name of the state
     * @param countyName the name of the county
     * @param httpClient the HTTP client (not used in the mock implementation)
     * @param cacher the cache object to potentially store or retrieve cached data (not used in the mock implementation)
     * @return a map containing the response data
     * @throws Exception if there are issues processing the request
     */
    @Override
    public Map<String, Object> processBroadbandRequest(String stateName, String countyName, HttpClient httpClient, BroadbandCache cacher) throws Exception {
        Map<String, Object> responseMap = new HashMap<>();

        if (stateName.equals("") || countyName.equals("") || stateName.isEmpty() || countyName.isEmpty()) {
            throw new IllegalArgumentException("State and county parameters are required.");
        }

        String stateCode = getStateCode(stateName);
        String countyCode = getCountyCode(stateCode, countyName, httpClient);

        String broadbandJson = fetchDataFromApi(stateCode, countyCode, httpClient);
        responseMap.put("state", stateName);
        responseMap.put("county", countyName);
        responseMap.put("result", "success");
        responseMap.put("data", broadbandJson);

        return responseMap;
    }
}
