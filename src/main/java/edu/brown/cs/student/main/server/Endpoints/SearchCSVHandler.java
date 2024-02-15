package edu.brown.cs.student.main.server.Endpoints;

import edu.brown.cs.student.main.parser.Search;
import edu.brown.cs.student.main.server.CSVHolder;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCSVHandler implements Route {

    private boolean convertToBoolean(String input){
        if(input.equals("true")){
            return true;
        }
        else if(input.equals("false")){
            return false;
        }
        else{
            return false;
        }
    }
    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        Map<String, Object> responseMap = new HashMap<>();
        String searchQuery = request.queryParams("query");
        String columnID = request.queryParams("column");
        String hasHeaders = request.queryParams("headers"); //defaults to false if the input is null or not a boolean value
        Boolean headers = this.convertToBoolean(hasHeaders);
        try {
            String csvFilePath = CSVHolder.getInstance().getCSVFilePath();

            if (csvFilePath != null && !csvFilePath.isEmpty()) {
                Search search = new Search(csvFilePath, searchQuery, columnID, headers);
                List<List<String>> searchResults = search.searchFor();

                responseMap.put("result", "success");
                responseMap.put("message", "Search operation completed.");
                responseMap.put("searchResults", "\n"+searchResults);
                response.status(200);
            } else {
                responseMap.put("result", "error");
                responseMap.put("message", "No CSV file is currently loaded!");
                response.status(400);
            }
        } catch (Exception e) {
            responseMap.put("result", "error");
            responseMap.put("message", e.getMessage());
            response.status(500);
        }
        return responseMap;
    }
}
