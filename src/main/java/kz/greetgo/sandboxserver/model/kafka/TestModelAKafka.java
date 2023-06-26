package kz.greetgo.sandboxserver.model.kafka;

import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;
import kz.greetgo.sandboxserver.model.mongo.TestModelADto;

public class TestModelAKafka {

  public String id;

  public ChangeVariant changeVariant;

  public String strField;

  public Boolean boolField;

  public Integer intField;

  public static TestModelAKafka fromDto(TestModelADto dto, ChangeVariant changeVariant) {
    TestModelAKafka kafka = new TestModelAKafka();

    kafka.id = dto.strId();
    kafka.changeVariant = changeVariant;
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
