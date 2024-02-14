package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.*;
import static org.testng.Assert.assertFalse;

import edu.brown.cs.student.main.FactoryFailureException;
import edu.brown.cs.student.main.parser.RowCreators.StandardObjectCreator;
import edu.brown.cs.student.main.parser.Search;
import edu.brown.cs.student.main.parser.CsvParser;
import java.io.*;
import java.util.List;
import org.testng.annotations.Test;

public class TestSuite {
  private static final String EARNINGS_DISPARITY_CSV = "data/census/dol_ri_earnings_disparity.csv";
  private static final String POSTSECONDARY_EDUCATION_CSV =
      "data/census/postsecondary_education.csv";
  private static final String INCOME_BY_RACE_CSV = "data/census/income_by_race.csv";
  private static final String MALFORMED_CSV = "data/malformed/malformed_signs.csv";
  private Reader reader;
  private CsvParser parser;
  private Search search;

  /** Test parsing with an empty CSV file. */
  @Test
  public void testEmptyFile() throws IOException, FactoryFailureException {
    reader = new FileReader("data/empty_csv.csv");
    parser = new CsvParser<>(reader, new StandardObjectCreator(), true);
    List<List<String>> result = parser.parse();
    assertTrue(result.isEmpty(), "Expected an empty list for an empty file");
  }

  /** Test parsing a single row without a header. */
  @Test
  public void testSingleRowWithoutHeader() throws IOException, FactoryFailureException {
    reader = new FileReader("data/single_row_csv.csv");
    parser = new CsvParser<>(reader, new StandardObjectCreator(), false);
    List<List<String>> result = parser.parse();
    assertEquals(1, result.size(), "Expected a single row");
    assertEquals(10, result.get(0).size(), "Expected 10 elements in the row");
  }

  /** Test parsing a CSV file for correct size. */
  @Test
  public void testParsingSize() throws IOException, FactoryFailureException {
    FileReader fileReader = new FileReader(POSTSECONDARY_EDUCATION_CSV);
    CsvParser parser = new CsvParser<>(fileReader, new StandardObjectCreator(), true);
    List<List<String>> records = parser.parse();
    assertFalse(records.isEmpty());
    assertEquals(records.get(0).size(), 10);
  }

  /** Test the row count in the parsed data. */
  @Test
  public void testRowCount() throws IOException, FactoryFailureException {
    reader = new FileReader(POSTSECONDARY_EDUCATION_CSV);
    parser = new CsvParser<>(reader, new StandardObjectCreator(), true);
    List<List<String>> records = parser.parse();
    assertEquals(17, records.size());
  }

  /** Test parsing specific rows and values. */
  @Test
  public void testRowParsing() throws IOException, FactoryFailureException {
    reader = new FileReader(POSTSECONDARY_EDUCATION_CSV);
    parser = new CsvParser<>(reader, new StandardObjectCreator(), true);
    List<List<String>> records = parser.parse();
    assertEquals(10, records.get(1).size());
    assertEquals("Asian", records.get(1).get(0));
  }

  /** Test parsing with malformed rows and header present. */
  @Test
  public void testMalformedRowsHeader() throws IOException, FactoryFailureException {
    reader = new FileReader(MALFORMED_CSV);
    parser = new CsvParser<>(reader, new StandardObjectCreator(), true);
    List<List<String>> records = parser.parse();
    assertEquals(records.size(), 9);
  }

  /** Test parsing with malformed rows and no header. */
  @Test
  public void testMalformedRowsNoHeader() throws IOException, FactoryFailureException {
    reader = new FileReader(MALFORMED_CSV);
    parser = new CsvParser<>(reader, new StandardObjectCreator(), false);
    List<List<String>> records = parser.parse();
    assertEquals(records.size(), 13);
  }

  /** Test printing out malformed rows from the CSV. */
  @Test
  public void testPrintMalformedRows() throws IOException, FactoryFailureException {
    reader = new FileReader(MALFORMED_CSV);
    parser = new CsvParser<>(reader, new StandardObjectCreator(), true);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    List<List<String>> records = parser.parse();
    System.setOut(System.out);

    String output = outContent.toString();
    assertTrue(output.contains("The following rows are malformed and are not searchable:"));
    assertTrue(output.contains("Gemini,Roberto,Nick"));
    assertTrue(output.contains("Virgo,"));
    assertTrue(output.contains("Libra,"));
    assertTrue(output.contains("Aquarius,"));
    outContent.close();
    assertNotNull(records, "Parsed records should not be null.");

    int expectedNumberOfValidRows = 9;
    assertEquals(
        expectedNumberOfValidRows,
        records.size(),
        "The number of valid parsed records should match the expected count.");
  }

  /** Test parsing with a StringReader to test another use case. */
  @Test
  public void testWithStringReader() throws Exception {
    String csvContent = "Name,Age,Location\nJohn Doe,29,Providence\nJane Smith,34,California";
    StringReader stringReader = new StringReader(csvContent);
    CsvParser<List<String>> parser =
        new CsvParser<>(stringReader, new StandardObjectCreator(), true);
    List<List<String>> records = parser.parse();
    assertNotNull(records);
    assertEquals(3, records.size());
    assertEquals("John Doe", records.get(1).get(0));
  }

  /** Test file access outside the protected directory. */
  @Test
  public void testAccessingFileOutsideProtectedDirectory()
      throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outContent));

    search = new Search("unauthorized_file.csv", "testValue", "", true);
    search.searchFor();

    String expectedMessage = "Error: Invalid file location";
    String actualMessage = outContent.toString();
    assertTrue(actualMessage.contains(expectedMessage));
    System.setErr(System.err);
  }

  /** Test searching with a valid column index. */
  @Test
  public void testSearchWithValidColumnIndex() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    search = new Search(EARNINGS_DISPARITY_CSV, "75%", "5", true);
    search.searchFor();

    System.setOut(System.out);
    assertTrue(
        outContent.toString().contains("[RI, White, \" $1,058.47 \", 395773.6521,  $1.00 , 75%]"));
  }

  /** Test searching with a valid column name. */
  @Test
  public void testSearchWithValidColumnName() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    search = new Search(INCOME_BY_RACE_CSV, "6122", "Household Income by Race Moe", true);
    search.searchFor();

    System.setOut(System.out);
    assertTrue(
        outContent
            .toString()
            .contains(
                "[0, Total, 2020, 2020, 85413, 6122, \"Bristol County, RI\", 05000US44001, bristol-county-ri]"));
  }

  /** Test searching without specifying a column ID. */
  @Test
  public void testSearchWithNoColumnID() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    search = new Search(EARNINGS_DISPARITY_CSV, "RI", null, true);
    search.searchFor();

    assertEquals(outContent.size(), 363); // total number of words in output (hand counted)
    System.setOut(System.out);
  }

  /** Test searching outside the column range. */
  @Test
  public void testSearchOutsideColumn() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    search = new Search(EARNINGS_DISPARITY_CSV, "75%", "4", true);
    search.searchFor();

    System.setOut(System.out);
    assertTrue(outContent.toString().contains("No matches were found in the specified columns"));
  }

  /** Test unbounded search that yields no results. */
  @Test
  public void testSearchUnboundedNoResults() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    search = new Search(EARNINGS_DISPARITY_CSV, "not found", null, true);
    search.searchFor();

    System.setOut(System.out);
    assertTrue(outContent.toString().contains("No matches were found"));
  }

  /** Test searching with an invalidly large column index. */
  @Test
  public void testSearchWithInvalidLargeColumnIndex() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outContent));

    search = new Search(POSTSECONDARY_EDUCATION_CSV, "tester", "100", true);
    search.searchFor();

    String expectedMessage = "Error: Specified column value was too large";
    String actualMessage = outContent.toString();
    assertTrue(actualMessage.contains(expectedMessage));
    System.setErr(System.err);
  }

  /** Test searching with an invalidly small (negative) column index. */
  @Test
  public void testSearchWithInvalidSmallColumnIndex() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outContent));

    search = new Search(POSTSECONDARY_EDUCATION_CSV, "tester", "-100", true);
    search.searchFor();

    String expectedMessage = "Error: Specified column value was too small";
    String actualMessage = outContent.toString();
    assertTrue(actualMessage.contains(expectedMessage));
    System.setErr(System.err);
  }

  /** Test searching with an invalid column name. */
  @Test
  public void testSearchWithInvalidColumnName() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outContent));

    search = new Search(POSTSECONDARY_EDUCATION_CSV, "searchTerm", "InvalidColumnName", true);
    search.searchFor();

    String expectedMessage = "Error: The specified column name does not exist.";
    String actualMessage = outContent.toString();
    assertTrue(actualMessage.contains(expectedMessage));
    System.setErr(System.err);
  }

  /** Test searching with an invalid CSV file path. */
  @Test
  public void testSearchWithInvalidCSV() throws IOException, FactoryFailureException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outContent));

    search = new Search("none", "searchTerm", null, true);
    search.searchFor();

    String expectedMessage = "Error: Invalid file location";
    String actualMessage = outContent.toString();
    assertTrue(actualMessage.contains(expectedMessage));
    System.setErr(System.err);
  }

  /**
   * Test for regex parsing errors with commas inside quoted elements.
   *
   * <p>This test fails because the regex is not perfect. This test is intended to show the errors
   * in the regex
   *
   * @throws IOException
   * @throws FactoryFailureException
   */
  @Test
  public void testRegexErrors() throws IOException, FactoryFailureException {
    String csvContent =
        "\"Name, With, Commas\",123,\"Address, With, Commas\"\n"
            + "\"Another, Name, With, Commas\",456,\"Another, Address, With, Commas\"";
    StringReader stringReader = new StringReader(csvContent);
    CsvParser<List<String>> parser =
        new CsvParser<>(stringReader, new StandardObjectCreator(), false);

    List<List<String>> records = parser.parse();
    assertEquals(2, records.size(), "There should be two records.");

    List<String> firstRecord = records.get(0);
    assertEquals("Name, With, Commas", firstRecord.get(0));
    assertEquals("123", firstRecord.get(1));
    assertEquals("Address, With, Commas", firstRecord.get(2));

    List<String> secondRecord = records.get(1);
    assertEquals("Another, Name, With, Commas", secondRecord.get(0));
    assertEquals("456", secondRecord.get(1));
    assertEquals("Another, Address, With, Commas", secondRecord.get(2));
  }
}
