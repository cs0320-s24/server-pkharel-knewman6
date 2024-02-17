package edu.brown.cs.student.main.server.Endpoints;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.parser.Search;
import edu.brown.cs.student.main.server.CSVHolder;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles search requests on the loaded CSV file. Allows users to search for data
 * within the file based on a specified query, column, and whether the file contains headers.
 * Responses are formatted in JSON and include the search results or an error message if applicable.
 */
public class SearchCSVHandler implements Route {

  /**
   * Converts a string input to a boolean value. Specifically used to parse the 'headers' parameter.
   *
   * @param input the string input representing a boolean value.
   * @return true if the input is "true", otherwise false.
   */
  private boolean convertToBoolean(String input) {
    return "true".equals(input);
  }

  /**
   * Processes the incoming search request and extracts parameters for the search query,
   * the column to search in, and whether the CSV has headers. Performs the search
   * and constructs a response indicating success or failure with the corresponding data or
   * a relevant error message.
   *
   * @param request the Spark request object, containing the search parameters.
   * @param response the Spark response object, used to set response metadata such as content type.
   * @return A JSON string representing the search results or an error message.
   */
  @Override
  public Object handle(Request request, Response response) {
    response.type("application/json");
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    String searchQuery = request.queryParams("query");
    String columnID = request.queryParams("column");
    String hasHeaders =
        request.queryParams(
            "headers"); // defaults to false if the input is null or not a boolean value
    Boolean headers = this.convertToBoolean(hasHeaders);
    responseMap.put("Parameters", "Query-" + searchQuery + ", Column Name-" + columnID + ", Headers-" + headers);

    if (searchQuery == null || columnID == null) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("message", "Query and column parameters are required.");
      response.status(400);
      return responseMap;
    }

    try {
      String csvFilePath = CSVHolder.getInstance().getCSVFilePath();
      if (csvFilePath != null && !csvFilePath.isEmpty()) {
        Search search = new Search(csvFilePath, searchQuery, columnID, headers);
        List<List<String>> searchResults = search.searchFor();
        responseMap.put("result", "success");
        responseMap.put("data", searchResults);
        response.status(200);
      } else {
        responseMap.put("result", "error_datasource");
        responseMap.put("message", "No CSV file is currently loaded!");
        response.status(400);
      }
    } catch (IllegalStateException e) {
      responseMap.put("result", "error_datasource");
      responseMap.put("message", e.getMessage());
      response.status(400);
    } catch (Exception e) {
      responseMap.put("result", "error_bad_request");
      responseMap.put("message", e.getMessage());
      response.status(500);
    }
    return adapter.toJson(responseMap).replace("\\\"","");
  }
}
