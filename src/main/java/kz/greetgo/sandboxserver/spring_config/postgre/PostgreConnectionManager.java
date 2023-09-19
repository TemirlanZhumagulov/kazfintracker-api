package kz.greetgo.sandboxserver.spring_config.postgre;

import kz.greetgo.sandboxserver.util.StrUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class PostgreConnectionManager {

  @Value("${sandbox.postgre.url}")
  private String url;

  @Value("${sandbox.postgre.username}")
  private String username;

  @Value("${sandbox.postgre.password}")
  private String password;

  public Connection getPostgresqlConnection() {
    try {
      return DriverManager.getConnection(StrUtils.getEnvOrDefault("SANDBOX_POSTGRE_URL", url), username, password);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
