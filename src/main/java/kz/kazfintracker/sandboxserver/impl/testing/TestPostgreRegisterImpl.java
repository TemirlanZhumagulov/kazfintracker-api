package kz.kazfintracker.sandboxserver.impl.testing;

import kz.kazfintracker.sandboxserver.register.testing.TestPostgreRegister;
import kz.kazfintracker.sandboxserver.spring_config.postgre.PostgreConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class TestPostgreRegisterImpl implements TestPostgreRegister {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private PostgreConnectionManager connectionManager;

  @Override
  public void createTable(String tableName) {
    String sql = "CREATE TABLE " + tableName + " (some_field varchar);";

    try (Connection connection = connectionManager.getPostgresqlConnection();
         Statement statement = connection.createStatement()) {
      statement.execute(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }

}
