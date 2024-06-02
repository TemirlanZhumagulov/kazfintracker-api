package kz.kazfintracker.sandboxserver.prod.kafka;

import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaProducer;
import kz.kazfintracker.sandboxserver.util.jackson.ObjectMapperHolder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerImpl implements KafkaProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public KafkaProducerImpl(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void send(String topic, KafkaEntity kafka) {
    if (kafka.changeVariant == null) {
      throw new RuntimeException("Change variant cannot be null");
    }

    kafkaTemplate.send(topic, ObjectMapperHolder.writeJson(kafka));
  }
}
