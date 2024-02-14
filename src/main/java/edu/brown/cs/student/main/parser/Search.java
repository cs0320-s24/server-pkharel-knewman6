package edu.brown.cs.student.main.parser;

import edu.brown.cs.student.main.FactoryFailureException;
import edu.brown.cs.student.main.parser.RowCreators.StandardObjectCreator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The search class is designed to facilitate searching through a csv file It supports specifying
 * whether the CSV has headers, the file name to search through the value to search for, and the
 * optional column ID to narrow down the searc
 */
public class Search {
    private String filename;
    private String searchFor;
    private String columnID;
    private Boolean hasHeaders;

    /**
     * constructor for a new Search object.
     *
     * @param filename   the name of the file to search through.
     * @param searchFor  the value to search for within the CSV.
     * @param columnID   the optional ID of the column to search, which may be a name or column index
     * @param hasHeaders flag indicating whether the CSV has header rows.
     */
    public Search(String filename, String searchFor, String columnID, Boolean hasHeaders) {
        this.filename = filename;
        this.searchFor = searchFor;
        this.columnID = columnID;
        this.hasHeaders = hasHeaders;
    }

    /**
     * Determines the index of the column based on the given identifier in the constructor. The
     * identifier can be an index or a string representing the header name. Called as a helper
     * function in searchFor
     *
     * @param data       the parsed CSV data as a list of lists of strings.
     * @param columnID   the identifier for the column.
     * @param hasHeaders flag indicating if the CSV data contains headers.
     * @return the index of the column if found, or -1 otherwise.
     * @throws IllegalArgumentException if the column identifier is invalid.
     */
    private int determineColumnIndex(List<List<String>> data, String columnID, boolean hasHeaders) {
        int columnIndex = -1;
        if (columnID != null && !columnID.isEmpty()) {
            try {
                columnIndex = Integer.parseInt(columnID);
            } catch (NumberFormatException e) {
                if (hasHeaders && !data.isEmpty()) {
                    List<String> headers = data.get(0);
                    for (String elem : headers) {
                        elem.toLowerCase();
                    }
                    columnID.toLowerCase();
                    columnIndex = headers.indexOf(columnID);
                    if (columnIndex == -1) { // column index remains unchanged, meaning it is not found
                        System.err.println("Error: The specified column name does not exist.");
                    }
                } else { // data does not have headers or the set is empty
                    System.err.println("Error: Invalid column identifier.");
                }
            }
        }
        return columnIndex;
    }

    /**
     * Prints the rows that match the search criteria, used as a helper in searchFor. Iterates through
     * each element of each row of the CSV file with for loops
     *
     * @param data            the parsed CSV data as a list of lists of strings.
     * @param columnIndex     the index of the column to search in.
     * @param columnSpecified a boolean indicating if a specific column was specified for the search.
     * @return true if matches are found, false otherwise, (used to tailor response)
     */
    private List<List<String>> getMatchingRows(
            List<List<String>> data, int columnIndex, boolean columnSpecified) {

        List<List<String>> matchingRows = new ArrayList<>();
        for (List<String> eachRow : data) {
            if (columnSpecified) {
                if (columnIndex >= 0 && columnIndex < eachRow.size()) {
                    if (eachRow.get(columnIndex).contains(searchFor)) {
                        matchingRows.add(eachRow);
                    }
                } else if (columnIndex >= eachRow.size()) {
                    System.err.println("Error: Specified column value was too large");
                    break;
                } else {
                    System.err.println("Error: Specified column value was too small");
                    break;
                }
            } else {
                for (String field : eachRow) {
                    if (field.contains(searchFor)) {
                        matchingRows.add(eachRow);
                        break;
                    }
                }
            }
        }
        return matchingRows;
    }

    /**
     * uses helper methods to loop through the parameter CSV, while also checking file name security
     * and responding to errors called from opening a file incorrectly
     */
    public List<List<String>> searchFor() throws IOException, FactoryFailureException {
      List<List<String>> resultList = new ArrayList<>();
        if (!this.filename.contains("data/")) {
            System.err.println("Error: Invalid file location");
            return resultList;
        }
        try (FileReader fileReader = new FileReader(this.filename)) {
            CsvParser<List<String>> parser =
                    new CsvParser<>(fileReader, new StandardObjectCreator(), this.hasHeaders);
            List<List<String>> parsedData = parser.parse();

            boolean columnSpecified = columnID != null && !columnID.isEmpty();
            int columnIndex = -1;
            try {
                columnIndex = determineColumnIndex(parsedData, columnID, this.hasHeaders);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                return resultList;
            }
            resultList = getMatchingRows(parsedData, columnIndex, columnSpecified);

            if (resultList.isEmpty()) {
                if (columnSpecified) {
                    System.out.println("No matches were found in the specified columns");
                } else {
                    System.out.println("No matches were found");
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: CSV file not found.");
        } catch (IOException e) {
            System.err.println("Error: An I/O error occurred while reading the file.");
        } catch (FactoryFailureException e) {
            System.err.println("Error: Failed to create object from row data.");
        }
        return resultList;
    }
}
