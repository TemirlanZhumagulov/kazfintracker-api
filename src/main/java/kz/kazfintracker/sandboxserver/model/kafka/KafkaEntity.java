package kz.kazfintracker.sandboxserver.model.kafka;


import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class KafkaEntity {
  public ChangeVariant changeVariant;

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
  public Object object;

  public static KafkaEntity from(Object object, ChangeVariant changeVariant) {
    KafkaEntity kafkaEntity = new KafkaEntity();
    kafkaEntity.object = object;
    kafkaEntity.changeVariant = changeVariant;
    return kafkaEntity;
  }
}
