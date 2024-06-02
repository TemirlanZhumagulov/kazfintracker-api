package kz.kazfintracker.sandboxserver.impl.testing;

import kz.kazfintracker.sandboxserver.config.TestConfig;
import kz.kazfintracker.sandboxserver.register.testing.TestSchedulerRegister;
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
