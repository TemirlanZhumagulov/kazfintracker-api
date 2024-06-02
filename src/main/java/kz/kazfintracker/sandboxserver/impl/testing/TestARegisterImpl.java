package kz.kazfintracker.sandboxserver.impl.testing;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.kafka.TestModelAKafka;
import kz.kazfintracker.sandboxserver.model.mongo.TestModelADto;
import kz.kazfintracker.sandboxserver.model.web.read.TestModelAToRead;
import kz.kazfintracker.sandboxserver.model.web.upsert.TestModelAToUpsert;
import kz.kazfintracker.sandboxserver.mongo.MongoAccess;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.register.testing.TestARegister;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaProducer;
import kz.kazfintracker.sandboxserver.util.Ids;
import kz.kazfintracker.sandboxserver.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Slf4j
public class TestARegisterImpl implements TestARegister {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public TestModelAToRead load(String id) {
    TestModelADto first = mongoAccess.testModelA().find(Filters.eq("_id", new ObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }

    return first.toRead();
  }

  @Override
  public String create(TestModelAToUpsert testModel) {
    log.info("Data is received: " + testModel.id + testModel.strField + testModel.boolField + testModel.intField);
    Validator.validate(testModel, true);

    TestModelADto dto = TestModelADto.from(Ids.generate(), testModel);
    log.info("Data is converted to DTO: " + dto.id + dto.strField + dto.boolField + dto.intField);

    mongoAccess.testModelA().insertOne(dto);

    kafkaProducer.send(KafkaTopics.TOPIC_MODEL_A, KafkaEntity.from(TestModelAKafka.fromDto(dto), ChangeVariant.CREATE));

    return dto.strId();
  }

  @Override
  public void update(TestModelAToUpsert testModel) {
    Validator.validate(testModel, false);

    List<Bson> updates = List.of(
      Updates.set(TestModelADto.Fields.strField, testModel.strField),
      Updates.set(TestModelADto.Fields.boolField, testModel.boolField),
      Updates.set(TestModelADto.Fields.intField, testModel.intField)
    );

    mongoAccess.testModelA().updateOne(Filters.eq("_id", testModel.objectId()), Updates.combine(updates));

    TestModelADto dto = mongoAccess.testModelA().find(Filters.eq("_id", testModel.objectId())).first();

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_MODEL_A, KafkaEntity.from(TestModelAKafka.fromDto(dto), ChangeVariant.UPDATE));
  }

  @Override
  public void delete(String id) {
    TestModelADto dto = mongoAccess.testModelA().findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_MODEL_A, KafkaEntity.from(TestModelAKafka.fromDto(dto), ChangeVariant.DELETE));
  }

}
