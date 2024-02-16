package edu.brown.cs.student.main.testing;

import edu.brown.cs.student.main.server.CSVHolder;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

public class TestLoadHolder {

  @Test
  public void testSingletonInstance() {
    CSVHolder instance1 = CSVHolder.getInstance();
    CSVHolder instance2 = CSVHolder.getInstance();
    Assert.assertSame(instance1, instance2);
  }

  @Test
  public void testLoadAndGetCSVFilePath() {
    CSVHolder csvHolder = CSVHolder.getInstance();
    String testPath = "path/to/test.csv";
    csvHolder.loadCSV(testPath);
    Assert.assertEquals(testPath, csvHolder.getCSVFilePath());
    String newTestPath = "different_path/to/test.csv";
    csvHolder.loadCSV(newTestPath);
    Assert.assertEquals(newTestPath, csvHolder.getCSVFilePath());
    Assert.assertNotEquals(testPath, csvHolder.getCSVFilePath());
  }

//  @Test(expected = IllegalStateException.class)
  public void testGetCSVFilePathWithoutLoading() {
    CSVHolder csvHolder = CSVHolder.getInstance();
    csvHolder.getCSVFilePath();
  }

  @Test
  public void testIsCSVLoaded() {
    CSVHolder csvHolder = CSVHolder.getInstance();
    String testPath = "src/main/java/edu/brown/cs/student/main/data/file.csv";
    Assert.assertFalse(csvHolder.isCSVLoaded());
    csvHolder.loadCSV(testPath);
    Assert.assertTrue(csvHolder.isCSVLoaded());
  }
}
