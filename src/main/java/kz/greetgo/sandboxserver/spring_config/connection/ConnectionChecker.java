package kz.greetgo.sandboxserver.spring_config.connection;

public interface ConnectionChecker {

  void check() throws NoConnection;

}
