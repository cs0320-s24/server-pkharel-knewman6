package edu.brown.cs.student.main.parser;

import edu.brown.cs.student.main.parser.RowCreators.CreatorFromRow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvParser<T> {
  private BufferedReader reader;
  private boolean hasHeaders;
  private CreatorFromRow<T> creator;

  /**
   * Generic CSV parser for converting CSV file content into a list of type T. It requires a Reader
   * to read the CSV file and a CreatorFromRow interface object to convert each CSV row into an
   * object of type T.
   *
   * @param <T> The type of objects that each CSV row will be converted into.
   */
  public CsvParser(Reader inputReader, CreatorFromRow<T> creator, Boolean hasHeaders) {
    this.reader = new BufferedReader(inputReader);
    this.creator = creator;
    this.hasHeaders = hasHeaders;
  }

  /**
   * Constructs a CsvParser with the provided reader, creator, and headers flag.
   *
   * @param inputReader Reader to read the CSV file.
   * @param creator CreatorFromRow instance to create objects of type T from each row.
   * @param hasHeaders Boolean flag indicating if the CSV file contains headers.
   */
  public List<T> parse() throws IOException, FactoryFailureException {
    List<T> parsedData = new ArrayList<>();
    String line = reader.readLine();
    List<String> headers = new ArrayList<>();
    if (line == null) {
      return parsedData;
    }
    if (hasHeaders) {
      // Split the header line to determine number of columns
      headers = Arrays.asList(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
    }
    List<String> malformedRows = new ArrayList<>(); // List to keep track of malformed rows
    while (line != null) {
      String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
      List<String> rowList = Arrays.asList(values);

      if (hasHeaders && (values.length != headers.size())) {
        malformedRows.add(line); // Add to malformed rows if the row size doesn't match header size
      } else {
        T row = creator.create(rowList); // Attempt to create a row
        parsedData.add(row);
      }
      line = reader.readLine();
    }
    if (!malformedRows.isEmpty()) {
//      System.out.println("The following rows are malformed and are not searchable:");
//      for (String row : malformedRows) {
//        System.out.println(row);
//      }
      //If malformed rows are desired, new functionality needs to be added to deal
    }
    return parsedData;
  }
}
