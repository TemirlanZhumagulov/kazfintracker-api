package kz.kazfintracker.sandboxserver.impl.crud;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.mappers.FinMapper;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.mongo.TransactionDto;
import kz.kazfintracker.sandboxserver.model.web.Transaction;
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
public class TransactionRegisterImpl implements CrudRegister<Transaction, Integer> {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public void create(Transaction transaction) {
    validate(transaction, true);

    mongoAccess.transaction().insertOne(FinMapper.INSTANCE.toDto(transaction));

    kafkaProducer.send(KafkaTopics.TOPIC_TRANSACTION, KafkaEntity.from(transaction, ChangeVariant.CREATE));
  }

  @Override
  public Transaction load(Integer id) {
    TransactionDto first = mongoAccess.transaction().find(Filters.eq("_id", Ids.intToObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }

    return FinMapper.INSTANCE.fromDto(first);
  }

  @Override
  public void update(Transaction transaction) {
    validate(transaction, false);

    List<Bson> updates = List.of(
      Updates.set(TransactionDto.Fields.date, transaction.date),
      Updates.set(TransactionDto.Fields.amount, transaction.amount),
      Updates.set(TransactionDto.Fields.type, transaction.type),
      Updates.set(TransactionDto.Fields.note, transaction.note),
      Updates.set(TransactionDto.Fields.idCategory, transaction.idCategory),
      Updates.set(TransactionDto.Fields.idBankAccount, transaction.idBankAccount),
      Updates.set(TransactionDto.Fields.idBankAccountTransfer, transaction.idBankAccountTransfer),
      Updates.set(TransactionDto.Fields.recurring, transaction.recurring),
      Updates.set(TransactionDto.Fields.recurrencyType, transaction.recurrencyType),
      Updates.set(TransactionDto.Fields.recurrencyPayDay, transaction.recurrencyPayDay),
      Updates.set(TransactionDto.Fields.recurrencyFrom, transaction.recurrencyFrom),
      Updates.set(TransactionDto.Fields.recurrencyTo, transaction.recurrencyTo),
      Updates.set(TransactionDto.Fields.createdAt, transaction.createdAt),
      Updates.set(TransactionDto.Fields.updatedAt, transaction.updatedAt)
    );

    mongoAccess.client().updateOne(Filters.eq("_id", Ids.intToObjectId(transaction.id)), Updates.combine(updates));

    kafkaProducer.send(KafkaTopics.TOPIC_TRANSACTION, KafkaEntity.from(transaction, ChangeVariant.UPDATE));
  }


  @Override
  public void delete(Integer id) {
    TransactionDto dto = mongoAccess.transaction().findOneAndDelete(Filters.eq("_id", Ids.intToObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_TRANSACTION, KafkaEntity.from(FinMapper.INSTANCE.fromDto(dto), ChangeVariant.DELETE));
  }

}
