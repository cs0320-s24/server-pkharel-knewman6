package edu.brown.cs.student.main.server.Endpoints;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.server.CSVHolder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {

  @Override
  public Object handle(Request request, Response response) {
    response.type("application/json");
    Map<String, Object> responseMap = new HashMap<>();

    try {
      CSVHolder csvHolder = CSVHolder.getInstance();
      String csvFilePath = csvHolder.getCSVFilePath();

      if (csvFilePath == null || csvFilePath.isEmpty()) {
        responseMap.put("result", "error");
        responseMap.put("message", "No CSV file is currently loaded!");
        response.status(400);
        return new CSVErrorResponse(responseMap).serialize();
      } else {
        List<String> csvLines = Files.readAllLines(Paths.get(csvFilePath));
        List<List<String>> jsonData = new ArrayList<>();

        for (String line : csvLines) {
          List<String> row = new ArrayList<>();
          String[] cells = line.split(",");
          for (String cell : cells) {
            row.add(cell.trim());
          }
          jsonData.add(row);
        }
        responseMap.put("result", "success");
        responseMap.put("message", "CSV content loaded successfully.");
        responseMap.put("data", jsonData);
        response.status(200);
        return new CSVSuccessResponse(responseMap).serialize();
      }
    } catch (IOException | IllegalStateException e) {
      responseMap.put("result", "error");
      responseMap.put("message", e.getMessage());
      response.status(500);
      return new CSVErrorResponse(responseMap).serialize();
    }
  }

  /** Response object for successful CSV content loading */
  public record CSVSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public CSVSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }

    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVSuccessResponse> adapter = moshi.adapter(CSVSuccessResponse.class,String.class,Integer.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object for failure in CSV content loading */
  public record CSVErrorResponse(String response_type, Map<String, Object> responseMap) {
    public CSVErrorResponse(Map<String, Object> responseMap) {
      this("error", responseMap);
    }

    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(CSVErrorResponse.class).toJson(this);
    }
  }
}
