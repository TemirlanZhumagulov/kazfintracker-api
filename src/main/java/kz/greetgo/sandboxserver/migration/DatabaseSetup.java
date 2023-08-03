package kz.greetgo.sandboxserver.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private final static String URL = "jdbc:postgresql://localhost:12218/sandbox_db";
    private final static String USERNAME = "postgres";
    private final static String PASSWORD = "pass123";

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Z2Jco7y9Wl :: ", e);
        }
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("V856Irv9m6 :: ", e);
        }
    }

    public static void createSourceTables() {
        try (Connection connection = getConnection()) {

            String createCharmTable = "CREATE TABLE IF NOT EXISTS charm (" +
                    "id SERIAL PRIMARY KEY," +
                    "name VARCHAR(50) UNIQUE" +
                    ")";

            String createClientTable = "CREATE TABLE IF NOT EXISTS client (" +
                    "id SERIAL PRIMARY KEY," +
                    "cia_id VARCHAR(50) UNIQUE," +
                    "surname VARCHAR(50) NOT NULL," +
                    "name VARCHAR(50) NOT NULL," +
                    "patronymic VARCHAR(50)," +
                    "gender VARCHAR(10)," +
                    "birth_date DATE," +
                    "charm_id INT," +
                    "FOREIGN KEY (charm_id) REFERENCES charm(id)" +
                    ")";

            String createClientAddrTable = "CREATE TABLE IF NOT EXISTS client_addr (" +
                    "id SERIAL PRIMARY KEY," +
                    "client_cia_id VARCHAR(50)," +
                    "type VARCHAR(10)," +
                    "street VARCHAR(50)," +
                    "house VARCHAR(50)," +
                    "flat VARCHAR(50)," +
                    "FOREIGN KEY (client_cia_id) REFERENCES client(cia_id)," +
                    "CONSTRAINT unique_client_cia_id_type_combination UNIQUE (client_cia_id, type)" +
                    ")";

            String createClientPhoneTable = "CREATE TABLE IF NOT EXISTS client_phone (" +
                    "id SERIAL PRIMARY KEY," +
                    "client_cia_id VARCHAR(50)," +
                    "number VARCHAR(50)," +
                    "type VARCHAR(10)," +
                    "FOREIGN KEY (client_cia_id) REFERENCES client(cia_id)," +
                    "CONSTRAINT unique_client_cia_id_type_number_combination UNIQUE (client_cia_id, type, number)" +
                    ")";

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createCharmTable);
                statement.executeUpdate(createClientTable);
                statement.executeUpdate(createClientAddrTable);
                statement.executeUpdate(createClientPhoneTable);
            }
        } catch (SQLException e) {
            throw new RuntimeException("23UYT3y1Tf :: ", e);
        }
    }

    public static void dropSourceTables() {
        try (Connection connection = getConnection()) {

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS client_phone");

                statement.executeUpdate("DROP TABLE IF EXISTS client_addr");

                statement.executeUpdate("DROP TABLE IF EXISTS client");

                statement.executeUpdate("DROP TABLE IF EXISTS charm");
            }

        } catch (SQLException e) {
            throw new RuntimeException("uwEe4PUY0J :: ", e);
        }
    }


    public static void createCiaMigrationTmpTables(String tmpClientTable, String tmpPhoneTable) {
        try (Connection connection = getConnection()) {

            String createClientTableSQL = "CREATE TABLE " + tmpClientTable + " ("
                    + "id SERIAL PRIMARY KEY,"
                    + "client_id VARCHAR(50),"
                    + "surname VARCHAR(50),"
                    + "name VARCHAR(50),"
                    + "patronymic VARCHAR(50),"
                    + "gender VARCHAR(10),"
                    + "charm VARCHAR(50),"
                    + "birth VARCHAR(50),"
                    + "fact_street VARCHAR(50),"
                    + "fact_house VARCHAR(50),"
                    + "fact_flat VARCHAR(50),"
                    + "register_street VARCHAR(50),"
                    + "register_house VARCHAR(50),"
                    + "register_flat VARCHAR(50),"
                    + "error VARCHAR(50) DEFAULT '',"
                    + "status VARCHAR(50) DEFAULT 'JUST_INSERTED'"
                    + ")";

            String createMobilePhoneTableSQL = "CREATE TABLE " + tmpPhoneTable + " ("
                    + "id SERIAL PRIMARY KEY,"
                    + "client_id VARCHAR(50),"
                    + "type VARCHAR(10),"
                    + "number VARCHAR(50),"
                    + "status VARCHAR(50) DEFAULT 'JUST INSERTED'"
                    + ")";

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createClientTableSQL);
                statement.executeUpdate(createMobilePhoneTableSQL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("EGIv4Wh718 :: ", e);
        }
    }
}
