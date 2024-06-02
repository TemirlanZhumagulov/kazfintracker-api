package kz.kazfintracker.sandboxserver.impl.crud;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.mappers.FinMapper;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.mongo.BudgetDto;
import kz.kazfintracker.sandboxserver.model.web.Budget;
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
public class BudgetRegisterImpl implements CrudRegister<Budget, Integer> {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public void create(Budget budget) {
    validate(budget, true);

    mongoAccess.budget().insertOne(FinMapper.INSTANCE.toDto(budget));

    kafkaProducer.send(KafkaTopics.TOPIC_BUDGET, KafkaEntity.from(budget, ChangeVariant.CREATE));
  }

  @Override
  public Budget load(Integer id) {
    BudgetDto first = mongoAccess.budget().find(Filters.eq("_id", Ids.intToObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }

    return FinMapper.INSTANCE.fromDto(first);
  }

  @Override
  public void update(Budget budget) {
    validate(budget, false);

    List<Bson> updates = List.of(
      Updates.set(BudgetDto.Fields.idCategory, budget.getIdCategory()),
      Updates.set(BudgetDto.Fields.name, budget.getName()),
      Updates.set(BudgetDto.Fields.amountLimit, budget.getAmountLimit()),
      Updates.set(BudgetDto.Fields.active, budget.getActive()),
      Updates.set(BudgetDto.Fields.createdAt, budget.getCreatedAt()),
      Updates.set(BudgetDto.Fields.updatedAt, budget.getUpdatedAt())
    );

    mongoAccess.budget().updateOne(Filters.eq("_id", Ids.intToObjectId(budget.getId())), Updates.combine(updates));

    kafkaProducer.send(KafkaTopics.TOPIC_BUDGET, KafkaEntity.from(budget, ChangeVariant.UPDATE));
  }

  @Override
  public void delete(Integer id) {
    BudgetDto dto = mongoAccess.budget().findOneAndDelete(Filters.eq("_id", Ids.intToObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_BUDGET, KafkaEntity.from(FinMapper.INSTANCE.fromDto(dto), ChangeVariant.DELETE));
  }
}
