package kz.greetgo.sandboxserver.migration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static kz.greetgo.sandboxserver.migration.util.TimeUtils.showTime;
import static org.assertj.core.api.Assertions.assertThat;

public class CiaMigrationTest {
    private Connection operConnection;

    private void info(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        System.out.println(sdf.format(new Date()) + " [" + getClass().getSimpleName() + "] " + message);
    }

    private void exec(String sql) throws SQLException {
        String executingSql = sql;

        long startedAt = System.nanoTime();
        try (Statement statement = operConnection.createStatement()) {
            int updates = statement.executeUpdate(executingSql);
            info("Updated " + updates
                    + " records for " + showTime(System.nanoTime(), startedAt)
                    + ", EXECUTED SQL : " + executingSql);
        } catch (SQLException e) {
            info("ERROR EXECUTE SQL for " + showTime(System.nanoTime(), startedAt)
                    + ", message: " + e.getMessage() + ", SQL : " + executingSql);
            throw e;
        }
    }

    @Test
    public void testClientSurnameIsNotDefined() throws SQLException {
        operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
        try {
            exec("INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) VALUES " +
                    "('c', '', 'n', 'p', 'g', 'ch', '2000-2-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')");
            exec("UPDATE client_tmp SET error = 'surname is not defined', status='ERROR' " +
                    "WHERE error = '' and (surname is null or surname = '')");
            try (Statement statement = operConnection.createStatement()) {
                //language=PostgreSQL
                ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp");
                while (resultSetC.next()) {
                    String status = resultSetC.getString("status");
                    String error = resultSetC.getString("error");
                    assertThat(status).isEqualTo("ERROR");
                    assertThat(error).isEqualTo("surname is not defined");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        operConnection.close();
    }

    @Test
    public void testClientNameIsNotDefined() throws SQLException {
        operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
        try {
            //language=Postgresql
            exec("INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) VALUES " +
                    "('c', 's', '', 'p', 'g', 'ch', '2000-2-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')");
            exec("UPDATE client_tmp SET error = 'name is not defined', status='ERROR' " +
                    "WHERE error = '' and (name is null or name = '')");
            try (Statement statement = operConnection.createStatement()) {
                //language=PostgreSQL
                ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp");
                while (resultSetC.next()) {
                    String status = resultSetC.getString("status");
                    String error = resultSetC.getString("error");
                    assertThat(status).isEqualTo("ERROR");
                    assertThat(error).isEqualTo("name is not defined");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        operConnection.close();
    }

    @Test
    public void testClientBirthDateIsNotDefined() throws SQLException {
        operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
        try {
            exec("INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) VALUES " +
                    "('c', 's', 'n', 'p', 'g', 'ch', '', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')");
            exec("UPDATE client_tmp SET error = 'birth_date is not defined', status='ERROR' " +
                    "WHERE error = '' and (birth is null or birth = '')");
            try (Statement statement = operConnection.createStatement()) {
                //language=PostgreSQL
                ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp");
                while (resultSetC.next()) {
                    String status = resultSetC.getString("status");
                    String error = resultSetC.getString("error");
                    assertThat(status).isEqualTo("ERROR");
                    assertThat(error).isEqualTo("birth_date is not defined");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        operConnection.close();

    }

    @Test
    public void testIsBirthDateValid() throws SQLException {
        operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
        try {
            // All
            //language=PostgreSQL
            exec("INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) VALUES " +
                    "('c', 's', 'n', 'p', 'g', 'ch', '2000-2-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '31-31-2022', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '1922-1-12', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '31-12-2000', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '31122022', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', 'laksdasld', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '12093101', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '2005-01-01', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '2005-07-03', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '1922-12-12', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '1922-07-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '1999-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '1978-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c3', 's', 'n', 'p', 'g', 'ch', '2002-01-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')");
            //language=PostgreSQL
            exec("UPDATE client_tmp SET error = 'birth_date is not correct', status='ERROR'" +
                    "WHERE NOT birth ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}$';");
            try (Statement statement = operConnection.createStatement()) {
                //language=PostgreSQL
                ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp WHERE client_id = 'c'");
                while (resultSetC.next()) {
                    String status = resultSetC.getString("status");
                    assertThat(status).isEqualTo("ERROR");
                }
                ResultSet resultSetC2 = statement.executeQuery("SELECT * FROM client_tmp WHERE client_id = 'c2'");
                while (resultSetC2.next()) {
                    String status = resultSetC2.getString("status");
                    assertThat(status).isEqualTo("JUST INSERTED");
                }
            }
            //language=PostgreSQL
            exec("DELETE FROM client_tmp WHERE client_id IN ('c', 'c2')");
        } catch (SQLException e) {
            e.printStackTrace();
            Assertions.fail("Encountered an exception while executing the queries.");
        }
        operConnection.close();
    }

    @Test
    public void isAgeRange_Between18and100() throws SQLException {
        operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
        try {
            LocalDate today = LocalDate.now();
            LocalDate tooYoung = today.minusYears(17);
            LocalDate notYet18ByMonth = today.minusYears(18).plusMonths(1);
            LocalDate notYet18ByDays = today.minusYears(18).plusDays(5);
            LocalDate tooOld = today.minusYears(102);
            LocalDate already101ByMonth = today.minusYears(101).minusMonths(1);
            LocalDate already101ByDays = today.minusYears(101).minusDays(5);

            LocalDate okOld = today.minusYears(100);
            LocalDate notYet101ByMonth = today.minusYears(101).plusMonths(1);
            LocalDate notYet101ByDays = today.minusYears(101).plusDays(5);
            LocalDate okYoung = today.minusYears(20);
            LocalDate already18ByMonth = today.minusYears(18).minusMonths(1);
            LocalDate already18ByDays = today.minusYears(18).minusDays(5);

            // client_id = c (Invalid Data)
            // client_id = c2 (Valid Data)
            //language=PostgreSQL
            exec("INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) VALUES " +
                    "('c', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(tooYoung) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(notYet18ByDays) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(notYet18ByMonth) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(tooOld) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(already101ByDays) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(already101ByMonth) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(okOld) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(notYet101ByMonth) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(notYet101ByDays) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(okYoung) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(already18ByMonth) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '" + java.sql.Date.valueOf(already18ByDays) + "', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')");
            //language=PostgreSQL
            exec("UPDATE client_tmp " +
                    " SET error = 'AGE_OUT_OF_RANGE', status = 'ERROR'" +
                    " WHERE error = '' and EXTRACT(YEAR FROM AGE(NOW(), TO_DATE(birth, 'YYYY-MM-DD'))) NOT BETWEEN 18 AND 100");

            try (Statement statement = operConnection.createStatement()) {
                //language=PostgreSQL
                ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp WHERE client_id = 'c'");
                while (resultSetC.next()) {
                    String status = resultSetC.getString("status");
                    assertThat(status).isEqualTo("ERROR");
                }
            }
            try (Statement statement = operConnection.createStatement()) {
                //language=PostgreSQL
                ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp WHERE client_id = 'c2'");
                while (resultSetC.next()) {
                    String status = resultSetC.getString("status");
                    assertThat(status).isEqualTo("JUST INSERTED");
                }
            }
            //language=PostgreSQL
            exec("DELETE FROM client_tmp WHERE client_id IN ('c', 'c2')");
        } catch (SQLException e) {
            e.printStackTrace();
            Assertions.fail("Encountered an exception while executing the queries.");
        }
        operConnection.close();
    }

    @Test
    public void should_Ignore_DuplicateClients() throws SQLException {
        operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
        try {
            // All
            //language=PostgreSQL
            exec("INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) VALUES " +
                    "('c', 's', 'n', 'p', 'g', 'ch', '2005-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '2005-7-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '1922-1-12', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '1922-2-27', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '2010-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 's', 'n', 'p', 'g', 'ch', '1-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c', 'not duplicate', 'n', 'p', 'g', 'ch', '1111-1-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '2005-1-1', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '2005-7-3', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '1922-12-12', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '1922-7-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 's', 'n', 'p', 'g', 'ch', '1999-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c2', 'not duplicate', 'n', 'p', 'g', 'ch', '1978-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                    "('c3', 'not duplicate', 'n', 'p', 'g', 'ch', '2002-1-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')");
            //language=PostgreSQL
            exec("UPDATE client_tmp AS c1 SET error = 'DUPLICATE', status = 'ERROR'\n" +
                    "FROM (\n" +
                    "    SELECT client_id, MAX(id) AS max_id\n" +
                    "    FROM client_tmp\n" +
                    "    WHERE status <> 'ERROR'" +
                    "    GROUP BY client_id\n" +
                    "    HAVING COUNT(*) > 1\n" +
                    ") AS c2\n" +
                    "WHERE c1.client_id = c2.client_id AND c1.id < c2.max_id;\n");
            try (Statement statement = operConnection.createStatement()) {
                //language=PostgreSQL
                ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp WHERE status = 'JUST INSERTED'");
                while (resultSetC.next()) {
                    String name = resultSetC.getString("surname");
                    assertThat(name).isEqualTo("not duplicate");
                }
            }
            //language=PostgreSQL
            exec("DELETE FROM client_tmp WHERE client_id IN ('c', 'c2')");
        } catch (SQLException e) {
            e.printStackTrace();
            Assertions.fail("Encountered an exception while executing the queries.");
        }
        operConnection.close();
    }

    @Test
    public void downloadTest() {
        String directoryPath = "src/test/java/org/example/testXml100";
        File outputDirectory = new File(directoryPath);
        File[] xmlFiles = outputDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (xmlFiles == null) {
            System.out.println("No XML files found in the directory.");
            return;
        }

        String insertClientSQL = "INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO phone_tmp (client_id, type, number) VALUES (?,?,?)";
        try (Connection connection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
             PreparedStatement ciaPS = connection.prepareStatement(insertClientSQL);
             PreparedStatement phonesPS = connection.prepareStatement(insertPhonesPS)) {
            long startedAt = System.nanoTime();
            int recordsCount = 0;
            connection.setAutoCommit(false);
            for (File xmlFile : xmlFiles) {
                System.out.println("Reading file: " + xmlFile.getName());
                FileInputStream fileInputStream = new FileInputStream(xmlFile);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                MySAXHandler handler = new MySAXHandler(connection, ciaPS, phonesPS, startedAt, recordsCount);
                saxParser.parse(fileInputStream, handler);

                fileInputStream.close();
            }
            connection.setAutoCommit(true);

            Statement statement = connection.createStatement();
            String sql = "SELECT count(*) FROM client_tmp";
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                assertThat(count).isEqualTo(100);
            }
            String sql2 = "SELECT count(*) FROM phone_tmp";
            ResultSet resultSet1 = statement.executeQuery(sql2);
            if(resultSet1.next()){
                int count = resultSet1.getInt(1);
                assertThat(count).isEqualTo(348);
            }

        } catch (IOException | ParserConfigurationException | SAXException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void migrateFromTmpTest() throws SQLException {
        operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
        //language=PostgreSQL
        exec("INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) VALUES " +
                "('c0', 's0', 'n', 'p', 'M', 'ch0', '1999-1-1', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c1', 's1', 'n', 'p', 'M', 'ch0', '2000-2-2', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c2', 's2', 'n', 'p', 'M', 'ch0', '2001-3-3', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c3', 's3', 'n', 'p', 'F', 'ch1', '2002-12-31', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c4', 's4', 'n', 'p', 'F', 'ch2', '2001-11-25', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c5', 's5', 'n', 'p', 'F', 'ch1', '2000-7-23', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c6', 's6', 'n', 'p', 'M', 'ch2', '1999-6-12', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c7', 's7', 'n', 'p', 'M', 'ch3', '1998-4-15', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c8', 's8', 'n', 'p', 'F', 'ch4', '1993-3-12', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c9', 's9', 'n', 'p', 'F', 'ch5', '1992-2-23', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')," +
                "('c10', 's10', 'n', 'p', 'F', 'ch1', '1943-1-11', 'f', 'f', 'f', 'r', 'r', 'r','','JUST INSERTED')"
        );
        //language=PostgreSQL
        exec("INSERT INTO phone_tmp (client_id, type, number) VALUES " +
                "('c0','HOME','1-2312-3-312')," +
                "('c1','MOBILE','1-2312-3-222')," +
                "('c2','MOBILE','1-1124-3-321')," +
                "('c3','MOBILE','1-3123-3-123')," +
                "('c4','MOBILE','1-1242-3-121')," +
                "('c5','MOBILE','1-4121-3-432')," +
                "('c0','MOBILE','1-4745-3-212')," +
                "('c0','WORK','1-4566-3-454')," +
                "('c1','HOME','1-2344-3-421')," +
                "('c2','HOME','1-1233-3-122')"
        );
        operConnection.close();
        operConnection = DatabaseSetup.dropCreateActualTables();
        //language=PostgreSQL
        exec("INSERT INTO charm (name) SELECT DISTINCT charm FROM client_tmp");
        //language=PostgreSQL
        exec("INSERT INTO client (id, surname, name, patronymic, gender, birth_date, charm_id) " +
                "SELECT client_id, surname, name, patronymic, gender, TO_DATE(birth, 'YYYY-MM-DD'), (SELECT id FROM charm WHERE name = charm) FROM client_tmp WHERE status != 'ERROR' " +
                "ON CONFLICT (id) DO UPDATE SET surname = excluded.surname, name = excluded.name, patronymic = excluded.patronymic, gender = excluded.gender, birth_date = excluded.birth_date, charm_id = excluded.charm_id;");
        //language=PostgreSQL
        exec("INSERT INTO client_phone (client, number, type) " +
                "SELECT client_id, number, type FROM phone_tmp WHERE client_id IN (SELECT id FROM client)");
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'FACT', fact_street, fact_house, fact_flat FROM client_tmp WHERE status != 'ERROR' " +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'REG', register_street, register_house, register_flat FROM client_tmp WHERE status != 'ERROR' " +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");

        try (Statement statement = operConnection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSet = statement.executeQuery("SELECT * FROM charm");
            List<String> charmNames = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                charmNames.add(name);
            }
            List<String> expectedCharmNames = Arrays.asList("ch5", "ch4", "ch0", "ch3", "ch1", "ch2");
            assertThat(charmNames).containsExactlyInAnyOrderElementsOf(expectedCharmNames);

            ResultSet resultSet2 = statement.executeQuery("SELECT * FROM client");
            while (resultSet2.next()) {
                String id = resultSet2.getString("id");
                String surname = resultSet2.getString("surname");
                String name = resultSet2.getString("name");
                String patronymic = resultSet2.getString("patronymic");
                String gender = resultSet2.getString("gender");
                LocalDate birthDate = resultSet2.getDate("birth_date").toLocalDate();
                long charmId = resultSet2.getLong("charm_id");

                Assertions.assertThat(id).isNotEmpty();
                Assertions.assertThat(surname).isNotEmpty();
                Assertions.assertThat(name).isNotEmpty();
                Assertions.assertThat(patronymic).isNotEmpty();
                Assertions.assertThat(gender).isIn("M", "F");
                Assertions.assertThat(birthDate).isNotNull();
                Assertions.assertThat(charmId).isGreaterThan(0L);
            }

            ResultSet resultSet3 = statement.executeQuery("SELECT * FROM client_addr");
            while (resultSet3.next()) {
                String client = resultSet3.getString("client");
                String type = resultSet3.getString("type");
                String street = resultSet3.getString("street");
                String house = resultSet3.getString("house");
                String flat = resultSet3.getString("flat");

                Assertions.assertThat(client).isNotEmpty();
                Assertions.assertThat(type).isIn("FACT", "REG");
                Assertions.assertThat(street).isNotEmpty();
                Assertions.assertThat(house).isNotEmpty();
                Assertions.assertThat(flat).isNotEmpty();
            }

            ResultSet resultSet4 = statement.executeQuery("SELECT * FROM client_phone");
            while (resultSet4.next()) {
                String client = resultSet4.getString("client");
                String type = resultSet4.getString("type");
                String number = resultSet4.getString("number");

                Assertions.assertThat(client).isNotEmpty();
                Assertions.assertThat(type).isIn("HOME", "MOBILE", "WORK");
                Assertions.assertThat(number).isNotEmpty();
            }
        }

        //language=PostgreSQL
        exec("INSERT INTO charm (name) SELECT DISTINCT charm FROM client_tmp " +
                "ON CONFLICT (name) DO NOTHING ");
        //language=PostgreSQL
        exec("INSERT INTO client (id, surname, name, patronymic, gender, birth_date, charm_id) " +
                "SELECT client_id, surname, name, patronymic, gender, TO_DATE(birth, 'YYYY-MM-DD'), (SELECT id FROM charm WHERE name = charm) FROM client_tmp WHERE status != 'ERROR' " +
                "ON CONFLICT (id) DO UPDATE SET surname = excluded.surname, name = excluded.name, patronymic = excluded.patronymic, gender = excluded.gender, birth_date = excluded.birth_date, charm_id = excluded.charm_id;");
        //language=PostgreSQL
        exec("INSERT INTO client_phone (client, number, type) " +
                "SELECT client_id, number, type FROM phone_tmp WHERE client_id IN (SELECT id FROM client)");
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'FACT', fact_street, fact_house, fact_flat FROM client_tmp WHERE status != 'ERROR' " +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'REG', register_street, register_house, register_flat FROM client_tmp WHERE status != 'ERROR' " +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
        try (Statement statement = operConnection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM charm");
            if(resultSet.next()) assertThat(resultSet.getInt(1)).isEqualTo(6);

            ResultSet resultSet2 = statement.executeQuery("SELECT count(*) FROM client_phone");
            if(resultSet2.next()) assertThat(resultSet2.getInt(1)).isEqualTo(20);

            ResultSet resultSet3 = statement.executeQuery("SELECT count(*) FROM client_addr");
            if(resultSet3.next()) assertThat(resultSet3.getInt(1)).isEqualTo(22);

            ResultSet resultSet4 = statement.executeQuery("SELECT count(*) FROM client");
            if(resultSet4.next()) assertThat(resultSet4.getInt(1)).isEqualTo(11);

        }
    }

}
