package kz.kazfintracker.sandboxserver.spring_config.connection.checkers;

import kz.kazfintracker.sandboxserver.elastic.ElasticWorker;
import kz.kazfintracker.sandboxserver.spring_config.connection.ConnectionChecker;
import kz.kazfintracker.sandboxserver.spring_config.connection.NoConnection;

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
