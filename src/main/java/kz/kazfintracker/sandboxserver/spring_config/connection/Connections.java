package kz.kazfintracker.sandboxserver.spring_config.connection;

import kz.kazfintracker.sandboxserver.elastic.ElasticWorker;
import kz.kazfintracker.sandboxserver.spring_config.connection.checkers.ConnectionChecker_Elastic;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Connections implements InitializingBean {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private ElasticWorker elasticWorker;

  private final ConnectionWaiter waiter = new ConnectionWaiter();

  @Override
  public void afterPropertiesSet() {
    waiter.add(new ConnectionChecker_Elastic(elasticWorker));
  }

  public void waitForAll() {
    waiter.waitForAll();
  }

}
