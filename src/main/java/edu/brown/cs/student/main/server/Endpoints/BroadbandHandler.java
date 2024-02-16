package edu.brown.cs.student.main.server.Endpoints;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.Caching.BroadbandCache;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  private static final Map<String, String> stateNameToCode = new HashMap<>();
  private final HttpClient httpClient = HttpClient.newHttpClient();

  static {
    try {
      initializeStateCodes(stateNameToCode);
    } catch (IOException | InterruptedException | URISyntaxException e) {
      e.printStackTrace();
    }
  }

  private static void initializeStateCodes(Map<String, String> mapToPopulate)
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
      mapToPopulate.put(stateName, stateCode);
    }
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
      String stateCode = getStateCode(stateName, stateNameToCode);
      String countyCode = getCountyCode(stateCode, countyName, httpClient);
      String broadbandJson = BroadbandCache.getData(stateCode, countyCode);
      if (broadbandJson == null) {
        broadbandJson = fetchDataFromApi(stateCode, countyCode, httpClient);
        BroadbandCache.putData(stateCode, countyCode, broadbandJson);
      }
      responseMap.put("result", "success");
      responseMap.put("data", broadbandJson);
      response.status(200);
    } catch (Exception e) {
      responseMap.put("result", "error");
      responseMap.put("message", e.getMessage());
      response.status(500);
    }

    return adapter.toJson(responseMap);
  }

  private String getStateCode(String stateName, Map<String, String> mapOfCodes)
      throws IllegalArgumentException {
    if (stateName.equals("*")) {
      return "*";
    }
    String stateCode = mapOfCodes.get(stateName);
    if (stateCode == null) {
      throw new IllegalArgumentException("State name not found: " + stateName);
    }
    return stateCode;
  }

  private String getCountyCode(String stateCode, String countyName, HttpClient client)
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

  private String fetchDataFromApi(String stateCode, String county, HttpClient client)
      throws URISyntaxException, IOException, InterruptedException {
    String baseUri = "https://api.census.gov/data/2021/acs/acs1/subject/variables";
    String queryParam = "?get=NAME,S2802_C03_022E&for=county:" + county + "&in=state:" + stateCode;
    URI uri = new URI(baseUri + queryParam);
    HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }
}
