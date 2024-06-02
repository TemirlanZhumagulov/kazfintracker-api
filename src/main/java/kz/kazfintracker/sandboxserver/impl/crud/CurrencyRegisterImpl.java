package kz.kazfintracker.sandboxserver.impl.crud;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import kz.kazfintracker.sandboxserver.exception.NoElementWasFoundException;
import kz.kazfintracker.sandboxserver.model.kafka.KafkaEntity;
import kz.kazfintracker.sandboxserver.register.kafka.KafkaTopics;
import kz.kazfintracker.sandboxserver.mappers.FinMapper;
import kz.kazfintracker.sandboxserver.model.kafka.ChangeVariant;
import kz.kazfintracker.sandboxserver.model.mongo.CurrencyDto;
import kz.kazfintracker.sandboxserver.model.web.Currency;
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
public class CurrencyRegisterImpl implements CrudRegister<Currency, Integer> {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private KafkaProducer kafkaProducer;

  @Override
  public void create(Currency currency) {
    validate(currency, true);

    mongoAccess.currency().insertOne(FinMapper.INSTANCE.toDto(currency));

    kafkaProducer.send(KafkaTopics.TOPIC_CURRENCY, KafkaEntity.from(currency, ChangeVariant.CREATE));
  }

  @Override
  public Currency load(Integer id) {
    CurrencyDto first = mongoAccess.currency().find(Filters.eq("_id", Ids.intToObjectId(id))).first();

    if (first == null) {
      throw new NoElementWasFoundException("Dto with ID " + id + " does not exist");
    }

    return FinMapper.INSTANCE.fromDto(first);
  }

  @Override
  public void update(Currency currency) {
    validate(currency, false);

    List<Bson> updates = List.of(
      Updates.set(CurrencyDto.Fields.symbol, currency.getSymbol()),
      Updates.set(CurrencyDto.Fields.code, currency.getCode()),
      Updates.set(CurrencyDto.Fields.name, currency.getName()),
      Updates.set(CurrencyDto.Fields.mainCurrency, currency.getMainCurrency())
//      Updates.set(CurrencyDto.Fields.createdAt, currency.getCreatedAt()),
//      Updates.set(CurrencyDto.Fields.updatedAt, currency.getUpdatedAt())
    );

    mongoAccess.currency().updateOne(Filters.eq("_id", Ids.intToObjectId(currency.getId())), Updates.combine(updates));

    kafkaProducer.send(KafkaTopics.TOPIC_CURRENCY, KafkaEntity.from(currency, ChangeVariant.UPDATE));
  }

  @Override
  public void delete(Integer id) {
    CurrencyDto dto = mongoAccess.currency().findOneAndDelete(Filters.eq("_id", Ids.intToObjectId(id)));

    if (dto == null) {
      return;
    }

    kafkaProducer.send(KafkaTopics.TOPIC_CURRENCY, KafkaEntity.from(FinMapper.INSTANCE.fromDto(dto), ChangeVariant.DELETE));
  }
}
