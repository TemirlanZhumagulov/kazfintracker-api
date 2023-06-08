package kz.greetgo.sandboxserver.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.greetgo.sandboxserver.exception.NoElementWasFoundException;
import kz.greetgo.sandboxserver.kafka.producer.KafkaProducer;
import kz.greetgo.sandboxserver.model.kafka.ChangeVariant;
import kz.greetgo.sandboxserver.model.kafka.TestModelAKafka;
import kz.greetgo.sandboxserver.model.mongo.TestModelADto;
import kz.greetgo.sandboxserver.model.web.read.TestModelAToRead;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.TestARegister;
import kz.greetgo.sandboxserver.util.IdGenerator;
import kz.greetgo.sandboxserver.util.Validator;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
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
    Validator.validateA(testModel, true);

    TestModelADto dto = TestModelADto.from(IdGenerator.generate(), testModel);

    mongoAccess.testModelA().insertOne(dto);

    kafkaProducer.sendModelA(TestModelAKafka.fromDto(dto, ChangeVariant.CREATE));

    return dto.strId();
  }

  @Override
  public void update(TestModelAToUpsert testModel) {
    Validator.validateA(testModel, false);

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

    kafkaProducer.sendModelA(TestModelAKafka.fromDto(dto, ChangeVariant.UPDATE));
  }

  @Override
  public void delete(String id) {
    TestModelADto dto = mongoAccess.testModelA().findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.sendModelA(TestModelAKafka.fromDto(dto, ChangeVariant.DELETE));
  }

}
