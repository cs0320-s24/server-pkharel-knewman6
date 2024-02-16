package edu.brown.cs.student.main.server.Endpoints.broadband;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.Caching.BroadbandCache;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  private static final Map<String, String> stateNameToCode = new HashMap<>();
  private HttpClient httpClient = HttpClient.newHttpClient();
  private BroadbandCache cacher = new BroadbandCache(10, TimeUnit.MINUTES, 10);

  static {
    try {
      BroadbandHelper.initializeStateCodes(stateNameToCode);
    } catch (IOException | InterruptedException | URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public BroadbandHandler(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public BroadbandHandler() {
    this(HttpClient.newHttpClient());
  }

  @Override
  public Object handle(Request request, Response response) {
    response.type("application/json");

    LocalDateTime queryDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedDateTime = queryDateTime.format(formatter);

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();

    responseMap.put("queryDateTime", formattedDateTime);

    String stateName = request.queryParams("state");
    String countyName = request.queryParams("county");
    responseMap.put("state", stateName);
    responseMap.put("county", countyName);

    if (stateName == null || countyName == null || stateName.isEmpty() || countyName.isEmpty()) {
      responseMap.put("result", "error");
      responseMap.put("message", "State and county parameters are required.");
      return responseMap;
    }


    try {
      String stateCode = BroadbandHelper.getStateCode(stateName, stateNameToCode);
      String countyCode = BroadbandHelper.getCountyCode(stateCode, countyName, httpClient);
      String broadbandJson = cacher.getData(stateCode, countyCode);
      if (broadbandJson == null) {
        broadbandJson = BroadbandHelper.fetchDataFromApi(stateCode, countyCode, httpClient);
        cacher.putData(stateCode, countyCode, broadbandJson);
      }
      responseMap.put("result", "success");
      responseMap.put("data", broadbandJson);
      response.status(200);
    } catch (Exception e) {
      responseMap.put("result", "error");
      responseMap.put("message", e.getMessage());
      response.status(500);
    }
    //BroadbandCache.printCache();
    return adapter.toJson(responseMap).replace("\\\"","");
  }

}
