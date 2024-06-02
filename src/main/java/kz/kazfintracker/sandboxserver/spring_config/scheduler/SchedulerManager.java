package kz.kazfintracker.sandboxserver.spring_config.scheduler;

import kz.greetgo.kafka.core.config.EventConfigStorageZooKeeper;
import kz.greetgo.kafka.core.config.ZooConnectParams;
import kz.greetgo.scheduling.HasScheduled;
import kz.greetgo.scheduling.collector.Task;
import kz.greetgo.scheduling.scheduler.Scheduler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static kz.greetgo.scheduling.collector.TaskCollector.newTaskCollector;
import static kz.greetgo.scheduling.scheduler.SchedulerBuilder.newSchedulerBuilder;

@Component
public class SchedulerManager implements DisposableBean {

  @Value("${sandbox.zookeeper.server}")
  private String zookeeperServer;

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  public List<HasScheduled> schedulers;

  private Scheduler scheduler;

  private final EventConfigStorageZooKeeper zookeeperConfigStorage = new EventConfigStorageZooKeeper(
    "sandbox/scheduler", () -> zookeeperServer, ZooConnectParams.builder().build()
  );

  @Override
  public void destroy() {
    zookeeperConfigStorage.close();
    if (scheduler != null) {
      scheduler.shutdown();
    }
  }

  public void start() {

    List<Task> taskList = new ArrayList<>();

    {
      var store = new SchedulerConfigStoreZookeeperBridge(zookeeperConfigStorage, "core/");
      //noinspection CollectionAddAllCanBeReplacedWithConstructor
      taskList.addAll(newTaskCollector()
        .setSchedulerConfigStore(store)
        .addControllers(new ArrayList<>(schedulers))
        .setConfigExtension(".scheduler.txt")
        .setConfigErrorsExtension(".scheduler.errors.txt")
        .getTasks());
    }

    scheduler = newSchedulerBuilder()
      .addTasks(taskList)
      .setThrowCatcher(Throwable::printStackTrace)
      .setPingDelayMillis(500)
      .build();

    scheduler.startup();

  }

}
