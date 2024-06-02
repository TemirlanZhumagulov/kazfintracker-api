package kz.kazfintracker.sandboxserver.model.kafka;

import kz.kazfintracker.sandboxserver.model.elastic.TestModelAElastic;
import kz.kazfintracker.sandboxserver.model.mongo.TestModelADto;

public class TestModelAKafka {

  public String id;

  public String strField;

  public Boolean boolField;

  public Integer intField;

  public static TestModelAKafka fromDto(TestModelADto dto) {
    TestModelAKafka kafka = new TestModelAKafka();

    kafka.id = dto.strId();
    kafka.strField = dto.strField;
    kafka.boolField = dto.boolField;
    kafka.intField = dto.intField;

    return kafka;
  }

  public TestModelAElastic toElastic() {
    TestModelAElastic elastic = new TestModelAElastic();

    elastic.id = id;
    elastic.strField = strField;

    return elastic;
  }

}
