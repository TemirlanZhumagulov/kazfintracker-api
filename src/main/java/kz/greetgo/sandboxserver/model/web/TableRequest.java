package kz.greetgo.sandboxserver.model.web;

import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;

import java.util.HashMap;
import java.util.Map;

public class TableRequest {

  public String strField;

  public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>();

    if (strField != null) {
      map.put(TestModelAElastic.Fields.strField, strField);
    }

    return map;
  }


}
