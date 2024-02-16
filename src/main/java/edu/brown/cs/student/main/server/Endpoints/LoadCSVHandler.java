package edu.brown.cs.student.main.server.Endpoints;

import edu.brown.cs.student.main.server.CSVHolder;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCSVHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String filePath = request.queryParams("file_path");
    Map<String, Object> responseMap = new HashMap<>();

    if (filePath == null || filePath.isEmpty()) {
      response.status(400);
      responseMap.put("result", "error");
      responseMap.put("message", "No file path provided");
      return responseMap;
    }

    File file = new File(filePath);
    if (!file.exists() || file.isDirectory()) {
      response.status(400);
      responseMap.put("result", "error");
      responseMap.put("message", "File does not exist or is a directory");
      return responseMap;
    }

    CSVHolder.getInstance().loadCSV(filePath);
    response.status(200); // HTTP 200 OK
    responseMap.put("result", "success");
    responseMap.put("message", "CSV file path loaded successfully");

    return responseMap;
  }
}
