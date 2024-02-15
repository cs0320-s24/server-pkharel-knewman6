package edu.brown.cs.student.main.parser;

import java.io.IOException;
import java.util.Scanner;

/** The Main class of our project. This is where execution begins. */
public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.println("Enter the CSV file path, or type 'exit' to quit:");
      String filename = scanner.nextLine();

      if ("exit".equalsIgnoreCase(filename)) {
        break;
      }

      System.out.println("Enter the search term:");
      String searchFor = scanner.nextLine();

      System.out.println("Enter the column ID (or leave blank for no specific column):");
      String columnID = scanner.nextLine();

      System.out.println("Does the CSV file have headers? (yes/no):");
      String hasHeadersInput = scanner.nextLine();
      Boolean hasHeaders = "yes".equalsIgnoreCase(hasHeadersInput);

      try {
        Search search = new Search(filename, searchFor, columnID, hasHeaders);
        search.searchFor();
        System.out.println();
        System.out.println();
      } catch (IOException | FactoryFailureException e) {
        System.err.println("An error occurred: " + e.getMessage());
      }
    }
    scanner.close();
  }

  private Main(String[] args) {}

  private void run() {
    // dear student: you can remove this. you can remove anything. you're in cs32. you're free!
    System.out.println(
        "Your horoscope for this project:\n"
            + "Entrust in the Strategy pattern, and it shall give thee the sovereignty to "
            + "decide and the dexterity to change direction in the realm of thy code.");
  }
}
