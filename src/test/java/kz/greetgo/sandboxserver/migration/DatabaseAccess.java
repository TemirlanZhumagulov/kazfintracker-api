package kz.greetgo.sandboxserver.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseAccess {
    private final Connection connection;
    public String tmpClientTable;
    public String tmpPhoneTable;

    public DatabaseAccess(Connection connection) {
        this.connection = connection;
    }


    public int getSourceCharmCountByName(String uniqueCharm) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM charm WHERE name = '" + uniqueCharm + "'");
            if (!resultSet.next()) {
                throw new AssertionError("No " + uniqueCharm + " name found in charm table");
            }
            return resultSet.getInt(1);
        }
    }

    public int getSourceCharmIdByName(String uniqueCharm) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT id FROM charm WHERE name = '" + uniqueCharm + "'");
            if (!resultSet.next()) {
                throw new AssertionError("No " + uniqueCharm + " name found in charm table");
            }
            return resultSet.getInt(1);
        }
    }

    public Map<String, String> getSourceClientByCiaId(String cia_id) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM client WHERE cia_id = '" + cia_id + "';")) {
            if (!resultSet.next()) {
                throw new AssertionError("client with id " + cia_id + " not found in client table");
            }
            int charm_id = resultSet.getInt("charm_id");

            resultMap.put("cia_id", resultSet.getString("cia_id"));
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
            resultMap.put("charm_id", resultSet1.getString("id"));
        }

        return resultMap;
    }

    public Map<String, String> getTmpClientById(int id) throws SQLException {
        return getTmpClient("id", String.valueOf(id));
    }
    public Map<String, String> getTmpClientByClientId(String clientId) throws SQLException {
        return getTmpClient("client_id", clientId);
    }
    private Map<String, String> getTmpClient(String idType, String idValue) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();
        String query = "SELECT * FROM " + tmpClientTable + " WHERE " + idType + " = '" + idValue +"'";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (!resultSet.next()) {
                throw new AssertionError("No client found in table: " + tmpClientTable + " with " + idType + ": " + idValue);
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
            resultMap.put("status", resultSet.getString("status"));
            resultMap.put("error", resultSet.getString("error"));
        }

        return resultMap;
    }

    public Map<String, String> getTmpPhoneById(int id) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tmpPhoneTable + " WHERE id = " + id)) {

            if (!resultSet.next()) {
                throw new AssertionError("No client found in table: " + tmpClientTable + " with id: " + id);
            }
            resultMap.put("client_id", resultSet.getString("client_id"));
            resultMap.put("type", resultSet.getString("type"));
            resultMap.put("number", resultSet.getString("number"));
            resultMap.put("status", resultSet.getString("status"));

        }
        return resultMap;
    }


    public Map<String, String> getSourceClientAddrByIdAndType(String id, String type) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();

        try (Statement statement = connection.createStatement()) {
            //language=PostgresSQL
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client_addr WHERE client_cia_id = '" + id + "' AND type ='" + type + "';");

            if (!resultSet.next()) {
                throw new AssertionError("address with id " + id + " and type " + type + "not found in client_addr table");
            }
            resultMap.put("client_id", resultSet.getString("client_cia_id"));
            resultMap.put("type", resultSet.getString("type"));
            resultMap.put("street", resultSet.getString("street"));
            resultMap.put("house", resultSet.getString("house"));
            resultMap.put("flat", resultSet.getString("flat"));
        }
        return resultMap;
    }

}
