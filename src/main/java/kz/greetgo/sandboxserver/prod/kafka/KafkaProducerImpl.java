package kz.greetgo.sandboxserver.prod.kafka;

import kz.greetgo.sandboxserver.kafka.KafkaTopics;
import kz.greetgo.sandboxserver.model.kafka.ClientKafka;
import kz.greetgo.sandboxserver.model.kafka.TestModelAKafka;
import kz.greetgo.sandboxserver.register.kafka.KafkaProducer;
import kz.greetgo.sandboxserver.util.jackson.ObjectMapperHolder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerImpl implements KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendModelA(TestModelAKafka modelA) {

        if (modelA.changeVariant == null) {
            throw new RuntimeException("Change variant cannot be null");
        }

        kafkaTemplate.send(KafkaTopics.TOPIC_MODEL_A, ObjectMapperHolder.writeJson(modelA));
    }

    @Override
    public void sendClient(ClientKafka clientKafka) {

        if (clientKafka.changeVariant == null) {
            throw new RuntimeException("Change variant cannot be null");
        }

        kafkaTemplate.send(KafkaTopics.TOPIC_CLIENT, ObjectMapperHolder.writeJson(clientKafka));
    }

}
