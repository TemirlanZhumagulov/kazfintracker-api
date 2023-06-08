package kz.greetgo.sandboxserver.impl;

import kz.greetgo.sandboxserver.config.TestConfig;
import kz.greetgo.sandboxserver.register.TestSchedulerRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestSchedulerRegisterImpl implements TestSchedulerRegister {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private TestConfig testConfig;

  @Override
  public String value() {
    return testConfig.value();
  }

}
