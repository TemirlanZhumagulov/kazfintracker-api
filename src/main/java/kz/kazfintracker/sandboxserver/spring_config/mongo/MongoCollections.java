package kz.kazfintracker.sandboxserver.spring_config.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import kz.kazfintracker.sandboxserver.mongo.MongoConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MongoCollections {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  protected MongoConnection mongoConnection;

  private final ConcurrentHashMap<Class<?>, MongoCollection<?>> operativeByClassMap = new ConcurrentHashMap<>();

  public <T> MongoCollection<T> getCollection(Class<T> aClass) {
    var ret = operativeByClassMap
      .computeIfAbsent(aClass,
        cl -> getCollectionFromDatabase(mongoConnection.database(), cl));

    //noinspection unchecked
    return (MongoCollection<T>) ret;
  }

  private <T> MongoCollection<T> getCollectionFromDatabase(MongoDatabase database, Class<T> aClass) {
    String collectionName = collectionName(aClass);
    return database.getCollection(collectionName, aClass);
  }

  public static String collectionName(Class<?> aClass) {
    String collectionName = aClass.getSimpleName();
    if (!collectionName.endsWith("Dto")) {
      throw new RuntimeException("435vtzS3zK :: Collection class must end with `Dto`");
    }
    collectionName = collectionName.substring(0, collectionName.length() - 3);

    return collectionName;
  }

}
