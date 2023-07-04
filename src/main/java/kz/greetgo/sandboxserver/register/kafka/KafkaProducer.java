package kz.greetgo.sandboxserver.register.kafka;

import kz.greetgo.sandboxserver.model.kafka.ClientKafka;
import kz.greetgo.sandboxserver.model.kafka.TestModelAKafka;

public interface KafkaProducer {

    void sendModelA(TestModelAKafka modelA);

    void sendClient(ClientKafka clientKafka);
}
