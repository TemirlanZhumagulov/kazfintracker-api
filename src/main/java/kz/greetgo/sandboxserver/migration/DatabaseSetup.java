package kz.greetgo.sandboxserver.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private final static String URL = "jdbc:postgresql://localhost:12218/sandbox_db";
    private final static String USERNAME = "postgres";
    private final static String PASSWORD = "pass123";

    public static Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static Connection dropCreateActualTables() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Connection connection;
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            dropActualTables(connection);
            createActualTables(connection);
            System.out.println("Actual Tables recreated");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void dropActualTables(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS client_phone");

            statement.executeUpdate("DROP TABLE IF EXISTS client_addr");

            statement.executeUpdate("DROP TABLE IF EXISTS client");

            statement.executeUpdate("DROP TABLE IF EXISTS charm");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static void createActualTables(Connection connection) {
        String createCharmTable = "CREATE TABLE charm (" +
                "id SERIAL PRIMARY KEY," +
                "name VARCHAR(50) UNIQUE" +
                ")";

        String createClientTable = "CREATE TABLE client (" +
                "id VARCHAR(50) PRIMARY KEY," +
                "surname VARCHAR(50) NOT NULL," +
                "name VARCHAR(50) NOT NULL," +
                "patronymic VARCHAR(50)," +
                "gender VARCHAR(10)," +
                "birth_date DATE," +
                "charm_id INT," +
                "FOREIGN KEY (charm_id) REFERENCES charm(id)" +
                ")";

        String createClientAddrTable = "CREATE TABLE client_addr (" +
                "client VARCHAR(50)," +
                "type VARCHAR(10)," +
                "street VARCHAR(50)," +
                "house VARCHAR(50)," +
                "flat VARCHAR(50)," +
                "FOREIGN KEY (client) REFERENCES client(id)" +
                ")";

        String createClientPhoneTable = "CREATE TABLE client_phone (" +
                "client VARCHAR(50)," +
                "number VARCHAR(50)," +
                "type VARCHAR(10)," +
                "FOREIGN KEY (client) REFERENCES client(id)" +
                ")";

        String addConstraintUnique = "ALTER TABLE client_addr " +
                "ADD CONSTRAINT unique_client_type_combination UNIQUE (client, type)";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createCharmTable);
            statement.executeUpdate(createClientTable);
            statement.executeUpdate(createClientAddrTable);
            statement.executeUpdate(createClientPhoneTable);
            statement.executeUpdate(addConstraintUnique);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static Connection dropCreateTables(String tmpClient, String tmpPhone) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Connection connection;
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            dropTables(connection);
            createTables(connection, tmpClient, tmpPhone);
            System.out.println("TMP tables created successfully.");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void dropTables(Connection connection) throws SQLException {
        String dropMobileTableSQL = "DROP TABLE IF EXISTS phone_tmp";
        String dropClientTableSQL = "DROP TABLE IF EXISTS client_tmp";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropMobileTableSQL);
            statement.executeUpdate(dropClientTableSQL);
        }
    }

    private static void createTables(Connection connection, String tmpClientTable, String tmpPhoneTable) throws SQLException {
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
                + "client_id VARCHAR(50),"
                + "type VARCHAR(10),"
                + "number VARCHAR(50)"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createClientTableSQL);
            statement.executeUpdate(createMobilePhoneTableSQL);
        }
    }
}
