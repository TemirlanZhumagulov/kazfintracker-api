package kz.kazfintracker.sandboxserver.consumer;

import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.model.elastic.TestModelAElastic;
import kz.kazfintracker.sandboxserver.model.kafka.TestModelAKafka;
import kz.kazfintracker.sandboxserver.register.testing.TestAElasticRegister;
import kz.kazfintracker.sandboxserver.util.jackson.ObjectMapperHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModelAElasticConsumer {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private TestAElasticRegister testAElasticRegister;

  @KafkaListener(id = "model-a-elastic-consumer", topics = KafkaTopics.TOPIC_MODEL_A, containerFactory = "containerFactory")
  public void consume(String value) {
    log.info("data is consumed from kafka: {}", value);

    KafkaEntity modelA = ObjectMapperHolder.readJson(value, KafkaEntity.class);

    TestModelAElastic modelAElastic = ((TestModelAKafka) modelA.object).toElastic();

    log.info("data is converted to elastic: {}{}{}", modelAElastic.id, modelAElastic.strField, modelA.changeVariant);

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
