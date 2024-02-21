package edu.brown.cs.student.main.server.Endpoints.broadband;
import edu.brown.cs.student.main.server.Caching.BroadbandCache;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class MockBroadbandHelper implements IBroadbandHelper {
    private Map<String, String> mockStateCodeMap = new HashMap<>();
    private Map<String, String> mockCountyCodeMap = new HashMap<>();
    private final String mockBroadbandData = "{\"data\": \"Mock broadband data\"}";

    public MockBroadbandHelper(){
        initializeStateCodes();
    }

    private void initializeMockData() {
        mockStateCodeMap.put("mockState", "01");
        mockCountyCodeMap.put("mockCounty", "001");
        System.out.println("added");
    }

    @Override
    public void initializeStateCodes() {
        this.initializeMockData();
    }

    @Override
    public String getStateCode(String stateName) throws IllegalArgumentException {
        String stateCode = mockStateCodeMap.get(stateName);
        if (stateCode == null) {
            throw new IllegalArgumentException("State name not found: " + stateName);
        }
        return stateCode;
    }

    @Override
    public String getCountyCode(String stateCode, String countyName, HttpClient client) throws IllegalArgumentException {
        String countyCode = mockCountyCodeMap.get(countyName);
        if (countyCode == null) {
            throw new IllegalArgumentException("County name not found: " + countyName);
        }
        return countyCode;
    }

    @Override
    public String fetchDataFromApi(String stateCode, String county, HttpClient client) {
        return mockBroadbandData;
    }

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
