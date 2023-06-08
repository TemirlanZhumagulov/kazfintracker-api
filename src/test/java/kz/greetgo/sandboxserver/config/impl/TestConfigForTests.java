package kz.greetgo.sandboxserver.config.impl;

import kz.greetgo.sandboxserver.config.TestConfig;
import org.springframework.stereotype.Component;

@Component
public class TestConfigForTests implements TestConfig {

  public String value;

  @Override
  public String value() {
    return value;
  }

}
