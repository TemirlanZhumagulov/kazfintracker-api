package kz.kazfintracker.sandboxserver.config.impl;

import kz.kazfintracker.sandboxserver.config.TestConfig;
import org.springframework.stereotype.Component;

@Component
public class TestConfigForTests implements TestConfig {

  public String value;

  @Override
  public String value() {
    return value;
  }

}
