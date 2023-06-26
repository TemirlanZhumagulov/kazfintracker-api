package kz.greetgo.sandboxserver.kafka.consumer;

import kz.greetgo.sandboxserver.kafka.KafkaTopics;
import kz.greetgo.sandboxserver.model.elastic.ClientElastic;
import kz.greetgo.sandboxserver.model.kafka.ClientKafka;
import kz.greetgo.sandboxserver.register.ClientElasticRegister;
import kz.greetgo.sandboxserver.util.jackson.ObjectMapperHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClientConsumer {
    @Autowired
    ClientElasticRegister clientElasticRegister;
    @KafkaListener(id = "client-elastic-consumer", topics = KafkaTopics.TOPIC_CLIENT, containerFactory = "containerFactory")
    public void consume(String value) {
        log.info("KAFKA CONSUMED A JSON VALUE: " + value);
        ClientKafka clientKafka = ObjectMapperHolder.readJson(value, ClientKafka.class);
        log.info("VALUE TRANSFORMED INTO CLIENT KAFKA CLASS: " + clientKafka);
        log.info("CLIENT BIRTHDATE: " + clientKafka.birth_date);
        ClientElastic clientElastic = clientKafka.toElastic();
        log.info("CLIENT TRANSFORMED INTO THE ELASTIC CLASS");
        switch (clientKafka.changeVariant) {
            case CREATE:
                clientElasticRegister.create(clientElastic);
                break;

            case UPDATE:
                clientElasticRegister.update(clientElastic);
                break;

            case DELETE:
                clientElasticRegister.delete(clientElastic.id);
                break;

            default:
                throw new RuntimeException("Unsupported type of changeVariant " + clientKafka.changeVariant);
        }

    }


}
