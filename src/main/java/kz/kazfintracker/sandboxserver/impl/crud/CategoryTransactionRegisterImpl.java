package kz.kazfintracker.sandboxserver.impl.crud;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.mappers.FinMapper;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.mongo.CategoryTransactionDto;
import kz.kazfintracker.sandboxserver.model.web.CategoryTransaction;
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
public class CategoryTransactionRegisterImpl implements CrudRegister<CategoryTransaction, Integer> {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public void create(CategoryTransaction categoryTransaction) {
    validate(categoryTransaction, true);

    mongoAccess.categoryTransaction().insertOne(FinMapper.INSTANCE.toDto(categoryTransaction));

    kafkaProducer.send(KafkaTopics.TOPIC_CATEGORY_TRANSACTION, KafkaEntity.from(categoryTransaction, ChangeVariant.CREATE));
  }

  @Override
  public CategoryTransaction load(Integer id) {
    CategoryTransactionDto first = mongoAccess.categoryTransaction().find(Filters.eq("_id", Ids.intToObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }

    return FinMapper.INSTANCE.fromDto(first);
  }

  @Override
  public void update(CategoryTransaction categoryTransaction) {
    validate(categoryTransaction, false);

    List<Bson> updates = List.of(
      Updates.set(CategoryTransactionDto.Fields.name, categoryTransaction.getName()),
      Updates.set(CategoryTransactionDto.Fields.symbol, categoryTransaction.getSymbol()),
      Updates.set(CategoryTransactionDto.Fields.color, categoryTransaction.getColor()),
      Updates.set(CategoryTransactionDto.Fields.note, categoryTransaction.getNote()),
      Updates.set(CategoryTransactionDto.Fields.parent, categoryTransaction.getParent()),
      Updates.set(CategoryTransactionDto.Fields.createdAt, categoryTransaction.getCreatedAt()),
      Updates.set(CategoryTransactionDto.Fields.updatedAt, categoryTransaction.getUpdatedAt())
    );

    mongoAccess.categoryTransaction().updateOne(Filters.eq("_id", Ids.intToObjectId(categoryTransaction.getId())), Updates.combine(updates));

    kafkaProducer.send(KafkaTopics.TOPIC_CATEGORY_TRANSACTION, KafkaEntity.from(categoryTransaction, ChangeVariant.UPDATE));
  }

  @Override
  public void delete(Integer id) {
    CategoryTransactionDto dto = mongoAccess.categoryTransaction().findOneAndDelete(Filters.eq("_id", Ids.intToObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_CATEGORY_TRANSACTION, KafkaEntity.from(FinMapper.INSTANCE.fromDto(dto), ChangeVariant.DELETE));
  }
}
