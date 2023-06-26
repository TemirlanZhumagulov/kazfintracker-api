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
    System.out.println("START TO CHECK ERRORS");
    checkerList.forEach(ConnectionChecker::check);
    System.out.println("FINISH TO CHECK ERORRS");
  }

  public void add(ConnectionChecker checker) {
    checkerList.add(checker);
  }

}
