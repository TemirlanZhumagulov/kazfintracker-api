package kz.greetgo.sandboxserver.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import kz.greetgo.sandboxserver.spring_config.mongo.codec.EnumCodecRegistry;
import kz.greetgo.sandboxserver.spring_config.mongo.codec.TimeZoneCodec;
import kz.greetgo.sandboxserver.util.StrUtils;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static kz.greetgo.sandboxserver.util.StrUtils.isNullOrBlank;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class MongoConnection implements InitializingBean, DisposableBean {

  @Value("${sandbox.mongo.server}")
  private String mongoServer;

  private MongoDatabase database;

  private MongoClient mongoClient;

  private static final String DB_NAME = "sandbox";

  public MongoDatabase database() {
    return database;
  }

  @Override
  public void afterPropertiesSet() {

    MongoClientSettings.Builder mcsBuilder = MongoClientSettings.builder()
            .codecRegistry(createCodecRegistry())
            .readPreference(readPreference());

    mongoClient = createMongoClient(StrUtils.getEnvOrDefault("SANDBOX_MONGO_SERVER", mongoServer), mcsBuilder);

    database = mongoClient.getDatabase(DB_NAME);
  }

  private CodecRegistry createCodecRegistry() {
    PojoCodecProvider.Builder pojoBuilder = PojoCodecProvider.builder();
    pojoBuilder.automatic(true);

    PojoCodecProvider pojoCodecProvider = pojoBuilder.build();

    CodecRegistry codecRegistry = MongoClientSettings.getDefaultCodecRegistry();

    EnumCodecRegistry enumCodecRegistry = new EnumCodecRegistry();

    return fromRegistries(
            TimeZoneCodec.REGISTRY,
            codecRegistry,
            fromProviders(pojoCodecProvider),
            enumCodecRegistry
    );
  }

  public static MongoClient createMongoClient(String server, MongoClientSettings.Builder mcsBuilder) {
    ConnectionString connectionString = createConStr(server);

    return MongoClients.create(mcsBuilder.applyConnectionString(connectionString).build());
  }

  private static ConnectionString createConStr(String server) {
    if (isNullOrBlank(server)) {
      throw new IllegalArgumentException("Mongo server location is not defined");
    }

    if (server.contains("://")) {
      return new ConnectionString(server);
    }

    return new ConnectionString("mongodb://" + server);
  }

  private ReadPreference readPreference() {
    return ReadPreference.primary();
  }

  @Override
  public void destroy() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

}
