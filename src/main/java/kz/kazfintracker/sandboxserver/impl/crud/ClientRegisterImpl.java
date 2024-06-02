package kz.kazfintracker.sandboxserver.impl.crud;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.kafka.ClientKafka;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.mongo.ClientDto;
import kz.kazfintracker.sandboxserver.model.web.read.ClientToRead;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.kazfintracker.sandboxserver.mongo.MongoAccess;
import kz.kazfintracker.sandboxserver.register.ClientRegister;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaProducer;
import kz.kazfintracker.sandboxserver.util.Ids;
import kz.kazfintracker.sandboxserver.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class ClientRegisterImpl implements ClientRegister {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public String create(ClientToUpsert client) {
    Validator.validate(client, true);

    ClientDto dto = ClientDto.from(Ids.generate(), client);
    mongoAccess.client().insertOne(dto);
    kafkaProducer.send(KafkaTopics.TOPIC_CLIENT, KafkaEntity.from(ClientKafka.fromDto(dto), ChangeVariant.CREATE));

    return dto.strId();
  }

  @Override
  public ClientToRead load(String id) {
    ClientDto first = mongoAccess.client().find(Filters.eq("_id", new ObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }
    return first.toRead();
  }

  @Override
  public String update(ClientToUpsert client) {
    String error = Validator.validate(client, false);
    if (error != null) return error;

    List<Bson> updates = List.of(
      Updates.set(ClientDto.Fields.name, client.getName()),
      Updates.set(ClientDto.Fields.surname, client.getSurname()),
      Updates.set(ClientDto.Fields.patronymic, client.getPatronymic()),
      Updates.set(ClientDto.Fields.gender, client.getGender()),
      Updates.set(ClientDto.Fields.birth_date, client.getBirth_date()),
      Updates.set(ClientDto.Fields.account, client.getAccount()),
      Updates.set(ClientDto.Fields.phones, client.getPhones()),
      Updates.set(ClientDto.Fields.addresses, client.getAddresses()),
      Updates.set(ClientDto.Fields.charm, client.getCharm())
    );

    mongoAccess.client().updateOne(Filters.eq("_id", client.objectId()), Updates.combine(updates));

    ClientDto dto = mongoAccess.client().find(Filters.eq("_id", client.objectId())).first();

    if (dto == null) {
      return null;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_CLIENT, KafkaEntity.from(ClientKafka.fromDto(dto), ChangeVariant.UPDATE));
    return dto.strId();
  }


  @Override
  public void delete(String id) {
    ClientDto dto = mongoAccess.client().findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_CLIENT, KafkaEntity.from(ClientKafka.fromDto(dto), ChangeVariant.DELETE));

  }

}
