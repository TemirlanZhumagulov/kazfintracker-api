package kz.greetgo.sandboxserver.kafka;

import kz.greetgo.sandboxserver.model.kafka.ClientKafka;
import kz.greetgo.sandboxserver.model.kafka.TestModelAKafka;
import kz.greetgo.sandboxserver.register.kafka.KafkaProducer;
import kz.greetgo.sandboxserver.util.jackson.ObjectMapperHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kz.greetgo.sandboxserver.kafka.KafkaTopics.TOPIC_CLIENT;
import static kz.greetgo.sandboxserver.kafka.KafkaTopics.TOPIC_MODEL_A;

@Component
public class KafkaProducerSimulator implements KafkaProducer, ApplicationContextAware {

    // key is topic, value is list of kafka records
    private final Map<String, List<String>> kafkaValues = new HashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void sendModelA(TestModelAKafka modelA) {

        if (modelA.changeVariant == null) {
            throw new RuntimeException("Change variant cannot be null");
        }

        String kafkaRecord = ObjectMapperHolder.writeJson(modelA);

        kafkaValues.computeIfAbsent(TOPIC_MODEL_A, ignored -> new ArrayList<>()).add(kafkaRecord);

    }

    @Override
    public void sendClient(ClientKafka client) {
        if (client.changeVariant == null) {
            throw new RuntimeException("Change variant cannot be null");
        }

        String kafkaRecord = ObjectMapperHolder.writeJson(client);

        kafkaValues.computeIfAbsent(TOPIC_CLIENT, ignored -> new ArrayList<>()).add(kafkaRecord);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void clear() {
        kafkaValues.clear();
    }

    public void push(Class<?> consumerClass) {
        Object consumerInstance = applicationContext.getBean(consumerClass);

        Method[] methods = ReflectionUtils.getAllDeclaredMethods(consumerClass);

        for (Method method : methods) {

            KafkaListener kafkaListener = method.getDeclaredAnnotation(KafkaListener.class);

            if (kafkaListener == null) {
                continue;
            }

            String[] topics = kafkaListener.topics();

            if (topics == null) {
                continue;
            }

            for (String topic : topics) {
                List<String> kafkaRecords = kafkaValues.get(topic);

                if (kafkaRecords == null) {
                    continue;
                }

                for (String kafkaRecord : kafkaRecords) {
                    ReflectionUtils.invokeMethod(method, consumerInstance, kafkaRecord);
                }

            }

        }

    }

}
