package kz.greetgo.sandboxserver.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.greetgo.sandboxserver.exception.NoElementWasFoundException;
import kz.greetgo.sandboxserver.kafka.producer.KafkaProducer;
import kz.greetgo.sandboxserver.model.kafka.ChangeVariant;
import kz.greetgo.sandboxserver.model.kafka.ClientKafka;
import kz.greetgo.sandboxserver.model.kafka.TestModelAKafka;
import kz.greetgo.sandboxserver.model.mongo.ClientDto;
import kz.greetgo.sandboxserver.model.mongo.TestModelADto;
import kz.greetgo.sandboxserver.model.web.read.ClientToRead;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.ClientRegister;
import kz.greetgo.sandboxserver.util.IdGenerator;
import kz.greetgo.sandboxserver.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
        ClientDto dto = ClientDto.from(IdGenerator.generate(), client);
        mongoAccess.client().insertOne(dto);
        kafkaProducer.sendClient(ClientKafka.fromDto(dto, ChangeVariant.CREATE));

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
    public void update(ClientToUpsert client) {
        Validator.validate(client, false);

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
            return;
        }

        kafkaProducer.sendClient(ClientKafka.fromDto(dto, ChangeVariant.UPDATE));

    }


    @Override
    public void delete(String id) {
        ClientDto dto = mongoAccess.client().findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

        if (dto == null) {
            return;
        }

        kafkaProducer.sendClient(ClientKafka.fromDto(dto, ChangeVariant.DELETE));

    }

}
