package kz.greetgo.sandboxserver.spring_config.connection.checkers;

import kz.greetgo.sandboxserver.elastic.ElasticWorker;
import kz.greetgo.sandboxserver.spring_config.connection.ConnectionChecker;
import kz.greetgo.sandboxserver.spring_config.connection.NoConnection;

public class ConnectionChecker_Elastic implements ConnectionChecker {

  private final ElasticWorker elasticWorker;

  public ConnectionChecker_Elastic(ElasticWorker elasticWorker) {
    this.elasticWorker = elasticWorker;
  }

  @Override
  public void check() throws NoConnection {
    try {
      boolean exists = elasticWorker.doesIndexExists("some_index");

      System.out.println("Index exists " + exists);
    } catch (RuntimeException e) {
      throw new NoConnection("ElasticSearch", e);
    }

  }

}
