package edu.brown.cs.student.main.server;

import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ViewCSVHandler implements Route {

    @Override
    public Object handle(Request request, Response response) throws IOException {
        List<List<String>> jsonData = new ArrayList<>();

        // Get CSV file path from CSVHolder
        CSVHolder csvHolder = CSVHolder.getInstance();
        String csvFilePath = csvHolder.getCSVFilePath();

        // Check if a CSV file is loaded
        if (csvFilePath == null || csvFilePath.isEmpty()) {
            return "{\"result\": \"error_bad_request\", \"message\": \"No CSV file loaded\"}";
        }

        // Read contents of the CSV file
        List<String> csvLines = Files.readAllLines(Paths.get(csvFilePath));

        // Prepare JSON response
        for (String line : csvLines) {
            List<String> row = new ArrayList<>();
            String[] cells = line.split(",");
            for (String cell : cells) {
                row.add(cell.trim()); // Trim to remove leading/trailing whitespace
            }
            jsonData.add(row);
        }

        // Serialize response to JSON and return
        return jsonData;
    }
}
