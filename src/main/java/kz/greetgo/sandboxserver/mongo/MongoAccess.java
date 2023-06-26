package kz.greetgo.sandboxserver.mongo;

import com.mongodb.client.MongoCollection;
import kz.greetgo.sandboxserver.model.mongo.ClientDto;
import kz.greetgo.sandboxserver.model.mongo.TestModelADto;
import kz.greetgo.sandboxserver.spring_config.mongo.MongoCollections;
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
}
