package kz.kazfintracker.sandboxserver.mongo;

import com.mongodb.client.MongoCollection;
import kz.kazfintracker.sandboxserver.elastic.ElasticWorker;
import kz.kazfintracker.sandboxserver.model.mongo.*;
import kz.kazfintracker.sandboxserver.model.web.CategoryTransaction;
import kz.kazfintracker.sandboxserver.model.web.RecurringTransactionAmount;
import kz.kazfintracker.sandboxserver.spring_config.mongo.MongoCollections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class MongoAccess {

  @Autowired
  protected MongoConnection mongoConnection;

  @Autowired
  protected MongoCollections collections;

  public MongoCollection<TestModelADto> testModelA() {
    return collections.getCollection(TestModelADto.class);
  }

  public MongoCollection<ClientDto> client() {
    return collections.getCollection(ClientDto.class);
  }

  public MongoCollection<TransactionDto> transaction() {
    return collections.getCollection(TransactionDto.class);
  }

  public MongoCollection<BankAccountDto> bankAccount() {
    return collections.getCollection(BankAccountDto.class);
  }

  public MongoCollection<RecurringTransactionAmountDto> recurringTransactionAmount() {
    return collections.getCollection(RecurringTransactionAmountDto.class);
  }

  public MongoCollection<CurrencyDto> currency() {
    return collections.getCollection(CurrencyDto.class);
  }

  public MongoCollection<BudgetDto> budget() {
    return collections.getCollection(BudgetDto.class);
  }

  public MongoCollection<CategoryTransactionDto> categoryTransaction() {
    return collections.getCollection(CategoryTransactionDto.class);
  }

}
