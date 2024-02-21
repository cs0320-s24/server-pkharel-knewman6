package edu.brown.cs.student.main.server.Endpoints.broadband;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.Caching.BroadbandCache;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles requests to fetch broadband data based on state and county parameters.
 * This class initializes state codes and sets up a caching mechanism to store
 * and retrieve broadband data efficiently. It supports handling requests with
 * or without an injected HttpClient instance for testing or customized HTTP behaviors.
 */
public class BroadbandHandler implements Route {

  private HttpClient httpClient = HttpClient.newHttpClient();
  private BroadbandCache cacher = new BroadbandCache(10, TimeUnit.MINUTES, 10);
  private IBroadbandHelper broadbandHelper = new BroadbandHelper();

  static {
    try {
      new BroadbandHelper().initializeStateCodes();
    } catch (IOException | InterruptedException | URISyntaxException e) {
      e.printStackTrace();
    }
  }

  /**
   * Constructs a BroadbandHandler with a custom HttpClient.
   * @param handler the interface BroadbandHelper to use for mocking external API requests.
   */
  public BroadbandHandler(IBroadbandHelper handler) {
    this.broadbandHelper = handler;
  }

  /**
   * Default constructor that uses the system's default HttpClient.
   */
  public BroadbandHandler() {}

  /**
   * Processes the incoming request to fetch broadband data for a given state and county.
   * The response includes the query's date and time and the fetched broadband data,
   * handling errors as necessary based on the request's validity and whether the API is accessed properly.
   *
   * Utilizes the BroadbandHelper helper methods
   *
   * @param request the Spark request object, containing query parameters.
   * @param response the Spark response object, used to set response metadata.
   * @return A JSON string representing the response to the client.
   */
  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

    String stateName = request.queryParams("state");
    String countyName = request.queryParams("county");

    try {
      Map<String, Object> result = broadbandHelper.processBroadbandRequest(stateName, countyName, httpClient, cacher);
      response.status(200);
      return adapter.toJson(result);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("result", "error");
      errorResponse.put("message", e.getMessage());
      response.status(500);
      return adapter.toJson(errorResponse);
    }
  }


}
