package kz.kazfintracker.sandboxserver.impl.crud;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.mappers.FinMapper;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.mongo.RecurringTransactionAmountDto;
import kz.kazfintracker.sandboxserver.model.web.RecurringTransactionAmount;
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
public class RecurringTransactionAmountRegisterImpl implements CrudRegister<RecurringTransactionAmount, Integer> {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public void create(RecurringTransactionAmount recurringTransactionAmount) {
    validate(recurringTransactionAmount, true);

    mongoAccess.recurringTransactionAmount().insertOne(FinMapper.INSTANCE.toDto(recurringTransactionAmount));

    kafkaProducer.send(KafkaTopics.TOPIC_RECUR_TRANSACTION, KafkaEntity.from(recurringTransactionAmount, ChangeVariant.CREATE));
  }

  @Override
  public RecurringTransactionAmount load(Integer id) {
    RecurringTransactionAmountDto first = mongoAccess.recurringTransactionAmount().find(Filters.eq("_id", Ids.intToObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }

    return FinMapper.INSTANCE.fromDto(first);
  }

  @Override
  public void update(RecurringTransactionAmount recurringTransactionAmount) {
    validate(recurringTransactionAmount, false);

    List<Bson> updates = List.of(
      Updates.set(RecurringTransactionAmountDto.Fields.from, recurringTransactionAmount.getFrom()),
      Updates.set(RecurringTransactionAmountDto.Fields.to, recurringTransactionAmount.getTo()),
      Updates.set(RecurringTransactionAmountDto.Fields.amount, recurringTransactionAmount.getAmount()),
      Updates.set(RecurringTransactionAmountDto.Fields.idTransaction, recurringTransactionAmount.getIdTransaction()),
      Updates.set(RecurringTransactionAmountDto.Fields.createdAt, recurringTransactionAmount.getCreatedAt()),
      Updates.set(RecurringTransactionAmountDto.Fields.updatedAt, recurringTransactionAmount.getUpdatedAt())
    );

    mongoAccess.recurringTransactionAmount().updateOne(Filters.eq("_id", Ids.intToObjectId(recurringTransactionAmount.getId())), Updates.combine(updates));

    kafkaProducer.send(KafkaTopics.TOPIC_RECUR_TRANSACTION, KafkaEntity.from(recurringTransactionAmount, ChangeVariant.UPDATE));
  }

  @Override
  public void delete(Integer id) {
    RecurringTransactionAmountDto dto = mongoAccess.recurringTransactionAmount().findOneAndDelete(Filters.eq("_id", Ids.intToObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_RECUR_TRANSACTION, KafkaEntity.from(FinMapper.INSTANCE.fromDto(dto), ChangeVariant.DELETE));
  }
}
