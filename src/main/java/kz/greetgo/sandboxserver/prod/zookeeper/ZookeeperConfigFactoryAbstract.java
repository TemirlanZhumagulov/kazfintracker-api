package kz.greetgo.sandboxserver.prod.zookeeper;

import kz.greetgo.conf.zookeeper.AbstractZookeeperConfigFactory;
import kz.greetgo.sandboxserver.config.TestConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperConfigFactoryAbstract extends AbstractZookeeperConfigFactory implements DisposableBean {

  @Value("${sandbox.zookeeper.server}")
  private String zookeeperServer;

  @Value("${sandbox.zookeeper.base-dir}")
  private String baseDir;

  @Override
  protected String baseDir() {
    return baseDir;
  }

  @Override
  protected String getConfigFileExt() {
    return ".txt";
  }

  @Override
  protected String zooConnectionString() {
    return zookeeperServer;
  }

  @Override
  public void destroy() {
    close();
  }

  @Override
  protected long autoResetTimeout() {
    return 2000;
  }

  @Bean
  public TestConfig testConfig() {
    return createConfig(TestConfig.class);
  }

}
