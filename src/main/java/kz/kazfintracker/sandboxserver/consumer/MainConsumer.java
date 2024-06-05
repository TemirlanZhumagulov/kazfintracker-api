package kz.kazfintracker.sandboxserver.consumer;

import kz.kazfintracker.sandboxserver.config.IgnoreKafkaExceptionConfig;
import kz.kazfintracker.sandboxserver.elastic.ElasticIndexes;
import kz.kazfintracker.sandboxserver.elastic.ElasticWorker;
import kz.kazfintracker.sandboxserver.model.elastic.ClientElastic;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.kafka.ClientKafka;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.web.*;
import kz.kazfintracker.sandboxserver.register.ClientElasticRegister;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.util.jackson.ObjectMapperHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static kz.kazfintracker.sandboxserver.util.jackson.ObjectMapperHolder.writeElastic;

@Component
@Slf4j
public class MainConsumer {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    ClientElasticRegister clientElasticRegister;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ElasticWorker elasticWorker;

    @Autowired
    private IgnoreKafkaExceptionConfig ignoreKafkaException;

    @KafkaListener(id = "client-elastic-consumer", topics = KafkaTopics.TOPIC_CLIENT, containerFactory = "containerFactory")
    public void consume(String value) {
        log.info("KAFKA CONSUMED A JSON VALUE: {}", value);

        KafkaEntity entity = ObjectMapperHolder.readJson(value, KafkaEntity.class);

        ClientElastic clientElastic = ((ClientKafka) entity.object).toElastic();

        switch (entity.changeVariant) {
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
                throw new RuntimeException("Unsupported type of changeVariant " + entity.changeVariant);
        }
    }

    @KafkaListener(id = "bank-account-elastic-consumer", topics = KafkaTopics.TOPIC_BANK_ACCOUNT, containerFactory = "containerFactory")
    public void bankAccountConsumer(String value) {
        try {
            log.info("BANK ACCOUNT CONSUMED: {}", value);

            KafkaEntity entity = ObjectMapperHolder.readJson(value, KafkaEntity.class);

            BankAccount bankAccount = (BankAccount) entity.object;
            sendToElastic(ElasticIndexes.INDEX_BANK_ACCOUNT, entity.changeVariant, String.valueOf(bankAccount.id), bankAccount);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @KafkaListener(id = "transaction-elastic-consumer", topics = KafkaTopics.TOPIC_TRANSACTION, containerFactory = "containerFactory")
    public void transactionConsumer(String value) {
        try {
            log.info("TRANSACTION CONSUMED: {}", value);

            KafkaEntity entity = ObjectMapperHolder.readJson(value, KafkaEntity.class);

            Transaction object = (Transaction) entity.object;
            sendToElastic(ElasticIndexes.INDEX_TRANSACTION, entity.changeVariant, String.valueOf(object.id), object);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @KafkaListener(id = "category-elastic-consumer", topics = KafkaTopics.TOPIC_CATEGORY_TRANSACTION, containerFactory = "containerFactory")
    public void categoryTransactionConsumer(String value) {
        try {
            log.info("CATEGORY CONSUMED: {}", value);

            KafkaEntity entity = ObjectMapperHolder.readJson(value, KafkaEntity.class);

            CategoryTransaction object = (CategoryTransaction) entity.object;
            sendToElastic(ElasticIndexes.INDEX_CATEGORY_TRANSACTION, entity.changeVariant, String.valueOf(object.id), object);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @KafkaListener(id = "budget-elastic-consumer", topics = KafkaTopics.TOPIC_BUDGET, containerFactory = "containerFactory")
    public void budgetConsumer(String value) {
        try {
            log.info("BUDGET CONSUMED: {}", value);

            KafkaEntity entity = ObjectMapperHolder.readJson(value, KafkaEntity.class);

            Budget object = (Budget) entity.object;
            sendToElastic(ElasticIndexes.INDEX_BUDGET, entity.changeVariant, String.valueOf(object.id), object);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @KafkaListener(id = "recur-transaction-consumer-consumer", topics = KafkaTopics.TOPIC_RECUR_TRANSACTION, containerFactory = "containerFactory")
    public void recurTransactionConsumer(String value) {
        try {
            log.info("RECURRING TRANSACTION CONSUMED: {}", value);

            KafkaEntity entity = ObjectMapperHolder.readJson(value, KafkaEntity.class);

            RecurringTransactionAmount object = (RecurringTransactionAmount) entity.object;
            sendToElastic(ElasticIndexes.INDEX_RECUR_TRANSACTION, entity.changeVariant, String.valueOf(object.id), object);
        } catch (Exception e) {
            handleException(e);
        }
    }


    @KafkaListener(id = "currency-consumer-consumer", topics = KafkaTopics.TOPIC_CURRENCY, containerFactory = "containerFactory")
    public void currencyConsumer(String value) {
        try {
            log.info("CURRENCY CONSUMED: {}", value);

            KafkaEntity entity = ObjectMapperHolder.readJson(value, KafkaEntity.class);

            Currency object = (Currency) entity.object;
            sendToElastic(ElasticIndexes.INDEX_CURRENCY, entity.changeVariant, String.valueOf(object.id), object);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        if (ignoreKafkaException.bankAccount()) {
            log.error(e.getMessage(), e);
            return;
        }

        throw new RuntimeException( e);
    }

    private void sendToElastic(String indexName, ChangeVariant changeVariant, String transactionId, Object object) {
        switch (changeVariant) {
            case CREATE:
                elasticWorker.insertDocument(indexName, String.valueOf(transactionId), writeElastic(object));
                break;
            case UPDATE:
                elasticWorker.updateDocument(indexName, String.valueOf(transactionId), writeElastic(object));
                break;
            case DELETE:
                elasticWorker.deleteDocument(indexName, transactionId);
                break;
            default:
                throw new RuntimeException("Unsupported type of changeVariant " + changeVariant);
        }
    }


}
