package edu.brown.cs.student.main.parser.RowCreators;

import edu.brown.cs.student.main.FactoryFailureException;
import java.util.ArrayList;
import java.util.List;

public class StandardObjectCreator implements CreatorFromRow<List<String>> {

  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    if (row == null) {
      throw new FactoryFailureException("Row data cannot be null", new ArrayList<>());
    }
    return row;
  }
}
