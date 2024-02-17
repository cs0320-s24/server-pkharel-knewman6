package edu.brown.cs.student.main.server;
/**
 * Singleton class responsible for managing the currently loaded CSV file within the application.
 * It keeps track of the file path of the loaded CSV and whether a CSV is currently loaded.
 */
public class CSVHolder {
  private static CSVHolder instance = null;
  private String csvFilePath;
  private boolean isCSVLoaded = false;

  /**
   * Private constructor to prevent instantiation from outside this class.
   */
  private CSVHolder() {}

  /**
   * Provides access to the singleton instance of the CSVHolder class,
   * creating it if it does not already exist.
   *
   * @return The singleton instance of the CSVHolder.
   */
  public static CSVHolder getInstance() {
    if (instance == null) {
      instance = new CSVHolder();
    }
    return instance;
  }

  /**
   * Loads a CSV file by storing its file path and marking the CSV as loaded.
   *
   * @param filePath The file path of the CSV to load.
   * @throws NullPointerException if the filePath argument is null.
   */
  public void loadCSV(String filePath) {
    if(filePath == null){
      throw new NullPointerException("filepath cannot be null");
    }
    this.csvFilePath = filePath;
    this.isCSVLoaded = true;
  }

  /**
   * Retrieves the file path of the currently loaded CSV file.
   *
   * @return The file path of the loaded CSV.
   * @throws IllegalStateException if no CSV file is currently loaded.
   */
  public String getCSVFilePath() throws IllegalStateException {
    if (!isCSVLoaded) {
      throw new IllegalStateException("No CSV file is currently loaded");
    }
    return csvFilePath;
  }

  /**
   * Unloads the currently loaded CSV file, clearing its file path and marking no CSV as loaded.
   */
  public void unloadCSV(){
    this.csvFilePath = null;
    this.isCSVLoaded = false;
  }

  /**
   * Checks whether a CSV file is currently loaded in the holder.
   *
   * @return True if a CSV is loaded, false otherwise.
   */
  public boolean isCSVLoaded() {
    return isCSVLoaded;
  }
}
