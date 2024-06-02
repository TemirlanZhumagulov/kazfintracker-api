package kz.kazfintracker.sandboxserver.config;

import kz.greetgo.conf.hot.DefaultStrValue;
import kz.greetgo.conf.hot.Description;

@Description("it is a test config")
public interface TestConfig {

  @Description("Value that will be printed out")
  @DefaultStrValue("It is value")
  String value();

}
