package kz.greetgo.sandboxserver.kafka.producer;

import kz.greetgo.sandboxserver.kafka.KafkaTopics;
import kz.greetgo.sandboxserver.model.kafka.TestModelAKafka;
import kz.greetgo.sandboxserver.util.jackson.ObjectMapperHolder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendModelA(TestModelAKafka modelA) {

    if (modelA.changeVariant == null) {
      throw new RuntimeException("Change variant cannot be null");
    }

    kafkaTemplate.send(KafkaTopics.TOPIC_MODEL_A, ObjectMapperHolder.writeJson(modelA));
  }

}
