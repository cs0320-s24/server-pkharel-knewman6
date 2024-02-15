package edu.brown.cs.student.main.server;

import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                response.status(400); // HTTP 400 Bad Request
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
            }
        } catch (IOException | IllegalStateException e) {
            responseMap.put("result", "error");
            responseMap.put("message", e.getMessage());
            response.status(500);
        }

        return responseMap;
    }
}
