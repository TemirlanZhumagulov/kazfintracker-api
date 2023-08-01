package kz.greetgo.sandboxserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseAccess {
    private final Connection connection;

    public DatabaseAccess(Connection connection) {
        this.connection = connection;
    }

    public int getRowCountFromTableWithCondition(String tableName, String condition) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            //language=PostgresSQL
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + tableName + " WHERE " + condition);
            if (!resultSet.next()) {
                throw new AssertionError("No client found in client table");
            }
            return resultSet.getInt(1);
        }
    }


    public int getCharmCountByName(String uniqueCharm) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            //language=PostgresSQL
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM charm WHERE name = '"+uniqueCharm+"'");
            if (!resultSet.next()) {
                throw new AssertionError("No client found in client table");
            }
            return resultSet.getInt(1);
        }
    }

    public Map<String, String> getClientById(String id) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM client WHERE id = '" + id + "';")) {
            if (!resultSet.next()) {
                throw new AssertionError("No client found in client table");
            }
            int charm_id = resultSet.getInt("charm_id");
            resultMap.put("id", resultSet.getString("id"));
            resultMap.put("surname", resultSet.getString("surname"));
            resultMap.put("birth_date", resultSet.getString("birth_date"));
            resultMap.put("name", resultSet.getString("name"));
            resultMap.put("patronymic", resultSet.getString("patronymic"));
            resultMap.put("gender", resultSet.getString("gender"));

            ResultSet resultSet1 = statement.executeQuery("SELECT * FROM charm WHERE id = " + charm_id);
            if (!resultSet1.next()) {
                throw new AssertionError("No name found in charm table with id: " + charm_id);
            }
            resultMap.put("charm", resultSet1.getString("name"));
        }

        return resultMap;
    }

    public Map<String, String> getClientFromTableWithCondition(String tableName, String condition) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE " + condition)) {

            if (!resultSet.next()) {
                throw new AssertionError("No client found in table: " + tableName);
            }

            resultMap.put("client_id", resultSet.getString("client_id"));
            resultMap.put("surname", resultSet.getString("surname"));
            resultMap.put("birth", resultSet.getString("birth"));
            resultMap.put("name", resultSet.getString("name"));
            resultMap.put("fact_street", resultSet.getString("fact_street"));
            resultMap.put("register_street", resultSet.getString("register_street"));
            resultMap.put("fact_house", resultSet.getString("fact_house"));
            resultMap.put("register_house", resultSet.getString("register_house"));
            resultMap.put("fact_flat", resultSet.getString("fact_flat"));
            resultMap.put("register_flat", resultSet.getString("register_flat"));
            resultMap.put("charm", resultSet.getString("charm"));
            resultMap.put("patronymic", resultSet.getString("patronymic"));
            resultMap.put("gender", resultSet.getString("gender"));
        }

        return resultMap;
    }

    public Map<String, String> getPhoneFromTableWithCondition(String tableName, String condition) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE " + condition)) {

            if (!resultSet.next()) {
                throw new AssertionError("No phone found in table: " + tableName);
            }
            resultMap.put("id", String.valueOf(resultSet.getInt("id")));
            resultMap.put("client_id", resultSet.getString("client"));
            resultMap.put("type", resultSet.getString("type"));
            resultMap.put("number", resultSet.getString("number"));
        }
        return resultMap;
    }

    public Map<String, String> getClientAddrByIdAndType(String id, String type) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();

        try (Statement statement = connection.createStatement()) {
            //language=PostgresSQL
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client_addr WHERE client = '" + id + "' AND type ='" + type + "';");

            if (!resultSet.next()) {
                throw new AssertionError("No client address found");
            }
            resultMap.put("client_id", resultSet.getString("client"));
            resultMap.put("type", resultSet.getString("type"));
            resultMap.put("street", resultSet.getString("street"));
            resultMap.put("house", resultSet.getString("house"));
            resultMap.put("flat", resultSet.getString("flat"));
        }
        return resultMap;
    }
}
