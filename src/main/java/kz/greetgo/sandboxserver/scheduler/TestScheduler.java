package kz.greetgo.sandboxserver.scheduler;

import kz.greetgo.sandboxserver.register.TestSchedulerRegister;
import kz.greetgo.scheduling.FromConfig;
import kz.greetgo.scheduling.HasScheduled;
import kz.greetgo.scheduling.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestScheduler implements HasScheduled {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private TestSchedulerRegister testSchedulerRegister;

  @FromConfig("Test schedule")
  @Scheduled("repeat every 1 minute")
  public void testSchedule() {
    System.out.println("FROM SCHEDULER: " + testSchedulerRegister.value());
  }

}
