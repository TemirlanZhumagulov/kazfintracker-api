package kz.kazfintracker.sandboxserver.spring_config.connection;

public class NoConnection extends RuntimeException {

  public NoConnection(String connectionName, Throwable connectionError) {
    super(connectionName + " : " + connectionError.getMessage(), connectionError);
  }

}
