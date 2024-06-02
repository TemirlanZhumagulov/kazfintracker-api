package kz.kazfintracker.sandboxserver.impl.crud;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.mappers.FinMapper;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.mongo.BankAccountDto;
import kz.kazfintracker.sandboxserver.model.web.BankAccount;
import kz.kazfintracker.sandboxserver.mongo.MongoAccess;
import kz.kazfintracker.sandboxserver.register.CrudRegister;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaProducer;
import kz.kazfintracker.sandboxserver.util.Ids;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static kz.kazfintracker.sandboxserver.util.Validator.validate;

@Slf4j
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class BankAccountRegisterImpl implements CrudRegister<BankAccount, Integer> {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public void create(BankAccount bankAccount) {
    validate(bankAccount, true);

    mongoAccess.bankAccount().insertOne(FinMapper.INSTANCE.toDto(bankAccount));

    kafkaProducer.send(KafkaTopics.TOPIC_BANK_ACCOUNT, KafkaEntity.from(bankAccount, ChangeVariant.CREATE));
}

  @Override
  public BankAccount load(Integer id) {
    BankAccountDto first = mongoAccess.bankAccount().find(Filters.eq("_id", Ids.intToObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }

    return FinMapper.INSTANCE.fromDto(first);
  }

  @Override
  public void update(BankAccount bankAccount) {
    validate(bankAccount, false);

    List<Bson> updates = List.of(
      Updates.set(BankAccountDto.Fields.name, bankAccount.name),
      Updates.set(BankAccountDto.Fields.symbol, bankAccount.symbol),
      Updates.set(BankAccountDto.Fields.color, bankAccount.color),
      Updates.set(BankAccountDto.Fields.startingValue, bankAccount.startingValue),
      Updates.set(BankAccountDto.Fields.active, bankAccount.active),
      Updates.set(BankAccountDto.Fields.mainAccount, bankAccount.mainAccount),
      Updates.set(BankAccountDto.Fields.createdAt, bankAccount.createdAt),
      Updates.set(BankAccountDto.Fields.updatedAt, bankAccount.updatedAt)
    );

    mongoAccess.bankAccount().updateOne(Filters.eq("_id", Ids.intToObjectId(bankAccount.id)), Updates.combine(updates));

    kafkaProducer.send(KafkaTopics.TOPIC_BANK_ACCOUNT, KafkaEntity.from(bankAccount, ChangeVariant.UPDATE));
  }

  @Override
  public void delete(Integer id) {
    BankAccountDto dto = mongoAccess.bankAccount().findOneAndDelete(Filters.eq("_id", Ids.intToObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_BANK_ACCOUNT, KafkaEntity.from(FinMapper.INSTANCE.fromDto(dto), ChangeVariant.DELETE));
  }

}
