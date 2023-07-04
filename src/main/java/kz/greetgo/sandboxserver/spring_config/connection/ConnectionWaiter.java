package kz.greetgo.sandboxserver.spring_config.connection;

import java.util.ArrayList;
import java.util.List;

public class ConnectionWaiter {
  private final List<ConnectionChecker> checkerList = new ArrayList<>();

  public void waitForAll() {
    while (true) {

      try {
        checkAllOrError();
        return;
      } catch (NoConnection ignored) {
      }

      try {
        //noinspection BusyWait
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        return;
      }

    }
  }

  public void checkAllOrError() {
    checkerList.forEach(ConnectionChecker::check);
  }

  public void add(ConnectionChecker checker) {
    checkerList.add(checker);
  }

}
