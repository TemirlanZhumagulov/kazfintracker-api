package kz.kazfintracker.sandboxserver.model.web;

import kz.kazfintracker.sandboxserver.model.elastic.TestModelAElastic;

import java.util.HashMap;
import java.util.Map;

public class TestTableRequest {

  public String strField;

  public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>();

    if (strField != null) {
      map.put(TestModelAElastic.Fields.strField, strField);
    }

    return map;
  }

}
