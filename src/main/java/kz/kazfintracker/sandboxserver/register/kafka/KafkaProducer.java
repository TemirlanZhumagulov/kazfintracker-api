package kz.kazfintracker.sandboxserver.register.kafka;

import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;

public interface KafkaProducer {

    void send(String topic, KafkaEntity kafka);

}
