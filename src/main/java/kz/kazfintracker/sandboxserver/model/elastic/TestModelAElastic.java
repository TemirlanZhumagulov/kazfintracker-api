package kz.kazfintracker.sandboxserver.model.elastic;

import kz.kazfintracker.sandboxserver.elastic.ElasticIndexes;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@FieldNameConstants
public class TestModelAElastic {

  public String id;

  public String strField;

  public static TestModelAElastic fromMap(Map<String, String> map) {
    TestModelAElastic model = new TestModelAElastic();

    model.id = map.get(Fields.id);
    model.strField = map.get(Fields.strField);

    return model;
  }

  public static String indexName() {
    return ElasticIndexes.INDEX_MODEL_A;
  }

  public static String mapping() {
    return "{\n" +
      "  \"mappings\": {\n" +
      "    \"properties\": {\n" +
      "      \"id\": {\n" +
      "        \"type\": \"keyword\"\n" +
      "      },\n" +
      "      \"strField\": {\n" +
      "        \"type\": \"text\"\n" +
      "      }\n" +
      "    }\n" +
      "  }\n" +
      "}";
  }


  @Override
  public String toString() {
    return "TestModelAElastic{" +
      "id='" + id + '\'' +
              ", strField='" + strField + '}';

    }
}
