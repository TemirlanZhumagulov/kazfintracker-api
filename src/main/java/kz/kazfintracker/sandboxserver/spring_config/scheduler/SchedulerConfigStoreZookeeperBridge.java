package kz.kazfintracker.sandboxserver.spring_config.scheduler;

import kz.greetgo.kafka.core.config.EventConfigStorageZooKeeper;
import kz.greetgo.scheduling.collector.SchedulerConfigStore;

import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SchedulerConfigStoreZookeeperBridge implements SchedulerConfigStore {

  private final EventConfigStorageZooKeeper storage;
  private final String                      prefix;

  public SchedulerConfigStoreZookeeperBridge(EventConfigStorageZooKeeper storage, String prefix) {
    this.storage = storage;
    this.prefix  = prefix;
  }

  @Override
  public boolean exists(String location) {
    return storage.exists(prefix(location));
  }

  private String prefix(String location) {
    return prefix == null ? location : prefix + location;
  }

  @Override
  public String getContent(String location) {
    byte[] bytes = storage.readContent(prefix(location));
    if (bytes == null) {
      return null;
    }
    return new String(bytes, UTF_8);
  }

  @Override
  public void setContent(String location, String content) {
    storage.writeContent(prefix(location), content == null ? null : content.getBytes(UTF_8));
  }

  @Override
  public long lastModifiedMillis(String location) {
    return storage.lastModifiedAt(prefix(location)).map(Date::getTime).orElse(0L);
  }

  @Override
  public String placeInfo(String location) {
    return "OnZookeeper{" + prefix(location) + "}";
  }

}
