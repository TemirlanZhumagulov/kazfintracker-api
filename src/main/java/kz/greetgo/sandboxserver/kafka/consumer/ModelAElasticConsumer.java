package kz.greetgo.sandboxserver.kafka.consumer;

import kz.greetgo.sandboxserver.kafka.KafkaTopics;
import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;
import kz.greetgo.sandboxserver.model.kafka.TestModelAKafka;
import kz.greetgo.sandboxserver.register.TestAElasticRegister;
import kz.greetgo.sandboxserver.util.jackson.ObjectMapperHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ModelAElasticConsumer {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private TestAElasticRegister testAElasticRegister;

  @KafkaListener(id = "model-a-elastic-consumer", topics = KafkaTopics.TOPIC_MODEL_A, containerFactory = "containerFactory")
  public void consume(String value) {
    TestModelAKafka modelA = ObjectMapperHolder.readJson(value, TestModelAKafka.class);

    TestModelAElastic modelAElastic = modelA.toElastic();

    switch (modelA.changeVariant) {
      case CREATE:
        testAElasticRegister.create(modelAElastic);
        break;

      case UPDATE:
        testAElasticRegister.update(modelAElastic);
        break;

      case DELETE:
        testAElasticRegister.delete(modelAElastic.id);
        break;

      default:
        throw new RuntimeException("Unsupported type of changeVariant " + modelA.changeVariant);
    }

  }

}
