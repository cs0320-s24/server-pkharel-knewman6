package edu.brown.cs.student.main.server.Endpoints.broadband;

import edu.brown.cs.student.main.server.Caching.BroadbandCache;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.Map;

public interface IBroadbandHelper {
    void initializeStateCodes() throws URISyntaxException, IOException, InterruptedException;
    String getStateCode(String stateName) throws IllegalArgumentException;
    String getCountyCode(String stateCode, String countyName, HttpClient client) throws IOException, InterruptedException, URISyntaxException, IllegalArgumentException;
    Map<String, Object> processBroadbandRequest(String stateName, String countyName, HttpClient httpClient, BroadbandCache cacher) throws Exception;
    String fetchDataFromApi(String stateCode, String county, HttpClient client) throws URISyntaxException, IOException, InterruptedException;
}
