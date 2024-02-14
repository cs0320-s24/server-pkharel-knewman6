package edu.brown.cs.student.main.server;

public class CSVHolder {
    private static CSVHolder instance = null;
    private String csvFilePath;
    private boolean isCSVLoaded = false;

    private CSVHolder() {}

    public static CSVHolder getInstance() {
        if (instance == null) {
            instance = new CSVHolder();
        }
        return instance;
    }

    public void loadCSV(String filePath) {
        this.csvFilePath = filePath;
        this.isCSVLoaded = true;
    }

    public String getCSVFilePath() throws IllegalStateException {
        if (!isCSVLoaded) {
            throw new IllegalStateException("No CSV file is currently loaded.");
        }
        return csvFilePath;
    }

    public boolean isCSVLoaded() {
        return isCSVLoaded;
    }
}
