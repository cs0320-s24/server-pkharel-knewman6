package edu.brown.cs.student.main.server;

import edu.brown.cs.student.main.FactoryFailureException;
import edu.brown.cs.student.main.parser.Search;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCSVHandler implements Route {

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        Map<String, Object> responseMap = new HashMap<>();
        String searchQuery = request.queryParams("query");
        String columnID = request.queryParams("column");

        try {
            String csvFilePath = CSVHolder.getInstance().getCSVFilePath();

            if (csvFilePath != null && !csvFilePath.isEmpty()) {
                Search search = new Search(csvFilePath, searchQuery, columnID, true);
                List<List<String>> searchResults = search.searchFor();

                // Add search results to the response map
                responseMap.put("result", "success");
                responseMap.put("message", "Search operation completed.");
                responseMap.put("searchResults", searchResults);
                response.status(200); // HTTP 200 OK
            } else {
                // No CSV file is loaded, return an error
                responseMap.put("result", "error");
                responseMap.put("message", "No CSV file is currently loaded.");
                response.status(400); // HTTP 400 Bad Request
            }
        } catch (IllegalStateException | FactoryFailureException e) {
            // Handle CSVHolder errors and FactoryFailureException
            responseMap.put("result", "error");
            responseMap.put("message", e.getMessage());
            response.status(500); // HTTP 500 Internal Server Error
        } catch (Exception e) {
            // Handle other exceptions
            responseMap.put("result", "error");
            responseMap.put("message", "An error occurred while processing the search.");
            response.status(500); // HTTP 500 Internal Server Error
        }

        // Convert responseMap to JSON
        return responseMap;
    }
}
