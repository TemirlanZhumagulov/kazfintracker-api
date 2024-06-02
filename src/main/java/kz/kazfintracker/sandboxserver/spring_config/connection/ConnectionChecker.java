package kz.kazfintracker.sandboxserver.spring_config.connection;

public interface ConnectionChecker {

  void check() throws NoConnection;

}
