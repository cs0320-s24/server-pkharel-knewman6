package edu.brown.cs.student.main.server;

import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LoadCSVHandler implements Route {
    private String loadedCSVContent;

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
