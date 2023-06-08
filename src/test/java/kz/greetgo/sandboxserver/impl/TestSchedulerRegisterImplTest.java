package kz.greetgo.sandboxserver.impl;

import kz.greetgo.sandboxserver.ParentTestNG;
import kz.greetgo.sandboxserver.config.impl.TestConfigForTests;
import kz.greetgo.sandboxserver.register.TestSchedulerRegister;
import kz.greetgo.util.RND;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSchedulerRegisterImplTest extends ParentTestNG {

  @Autowired
  private TestSchedulerRegister testSchedulerRegister;

  @Autowired
  private TestConfigForTests testConfigForTests;

  @Test
  public void create() {

    String suppliedValue = RND.strEng(10);

    testConfigForTests.value = suppliedValue;

    //
    //
    String valueFromConfig = testSchedulerRegister.value();
    //
    //

    assertThat(valueFromConfig).isEqualTo(suppliedValue);

  }

}
