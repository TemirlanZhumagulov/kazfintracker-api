package kz.greetgo.sandboxserver.migration;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static kz.greetgo.sandboxserver.migration.util.TimeUtils.showTime;
import static org.assertj.core.api.Assertions.assertThat;

public class CiaMigrationTest {
    private Connection operConnection;

    private void info(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        System.out.println(sdf.format(new java.util.Date()) + " [" + getClass().getSimpleName() + "] " + message);
    }

    private void exec(String sql) throws SQLException {

        long startedAt = System.nanoTime();
        try (Statement statement = operConnection.createStatement()) {
            int updates = statement.executeUpdate(sql);
            info("Updated " + updates
                    + " records for " + showTime(System.nanoTime(), startedAt)
                    + ", EXECUTED SQL : " + sql);
        } catch (SQLException e) {
            info("ERROR EXECUTE SQL for " + showTime(System.nanoTime(), startedAt)
                    + ", message: " + e.getMessage() + ", SQL : " + sql);
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
        String xmlData = "<cia>\n" +
                "  <client id=\"7-Y7H-26-CI-rLfpdWEkXJ\"> <!-- 1 -->\n" +
                "    <workPhone>+7-201-918-64-13 вн. afm3</workPhone>\n" +
                "    <birth value=\"1972-08-06\"/>\n" +
                "    <mobilePhone>+7-867-609-72-85</mobilePhone>\n" +
                "    <charm value=\"ve3JGxjp2d\"/>\n" +
                "    <homePhone>+7-194-353-47-03</homePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"jGq1cYN750zzqQwISySQ\" house=\"7v\" flat=\"zL\"/>\n" +
                "      <register street=\"xMrGcYpmI9rHHXGBxz83\" house=\"cz\" flat=\"GX\"/>\n" +
                "    </address>\n" +
                "    <name value=\"E8WM1BXEjq\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <surname value=\"18JHY1qHbr\"/>\n" +
                "    <mobilePhone>+7-890-371-42-27</mobilePhone>\n" +
                "    <patronymic value=\"vX3qMhR99VCjN\"/>\n" +
                "    <workPhone>+7-877-942-69-25</workPhone>\n" +
                "  </client>\n" +
                "  <client id=\"7-Y7H-26-CI-rLfpdWEkXJ\"> <!-- 2 -->\n" +
                "    <surname value=\"P0qBbgFtiW\"/>\n" +
                "    <workPhone>+7-632-501-85-36</workPhone>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <workPhone>+7-300-269-99-80</workPhone>\n" +
                "    <address>\n" +
                "      <fact street=\"mne2F8DUAO5ICkv5Dtfj\" house=\"h7\" flat=\"T9\"/>\n" +
                "      <register street=\"vLJMLEVrMC90xpokaGGK\" house=\"eX\" flat=\"VW\"/>\n" +
                "    </address>\n" +
                "    <charm value=\"b5rZizXFEq\"/>\n" +
                "    <mobilePhone>+7-063-440-44-00</mobilePhone>\n" +
                "    <patronymic value=\"RkosC1DjhGPEW\"/>\n" +
                "    <birth value=\"1940-06-13\"/>\n" +
                "    <mobilePhone>+7-128-758-75-73</mobilePhone>\n" +
                "    <name value=\"6GL5xBURZk\"/>\n" +
                "  </client>\n" +
                "  <client id=\"3-XHB-8X-JX-ETpnh4wZDY\"> <!-- 3 -->\n" +
                "    <surname value=\"X8FrtWgKxP\"/>\n" +
                "    <birth value=\"1960-06-13\"/>\n" +
                "    <name value=\"vhGgW8i0zC\"/>\n" +
                "    <patronymic value=\"AjPcNs5Qi0Epn\"/>\n" +
                "    <workPhone>+7-272-461-80-27</workPhone>\n" +
                "    <workPhone>+7-289-664-47-20</workPhone>\n" +
                "    <address>\n" +
                "      <fact street=\"hCbZa4y7PYj5wCbgbXh1\" house=\"3r\" flat=\"oF\"/>\n" +
                "      <register street=\"7nQEG2FfAXjw2mE74E3S\" house=\"DB\" flat=\"x3\"/>\n" +
                "    </address>\n" +
                "    <charm value=\"AQ15gFu3cO\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <homePhone>+7-886-499-64-35</homePhone>\n" +
                "  </client>\n" +
                "  <client id=\"7-LRE-GU-9G-Z3zBcBL4qK\"> <!-- 4 -->\n" +
                "    <patronymic value=\"GS2TMRDw9gynz\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"rY4lkbv9PETyllIWhO7v\" house=\"C1\" flat=\"po\"/>\n" +
                "      <register street=\"yjvFXrSg4rSe1KSv4jjb\" house=\"D3\" flat=\"EP\"/>\n" +
                "    </address>\n" +
                "    <workPhone>+7-730-691-98-26</workPhone>\n" +
                "    <surname value=\"kenCeQhWdt\"/>\n" +
                "    <mobilePhone>+7-457-764-80-82 вн. 6yyg</mobilePhone>\n" +
                "    <name value=\"\"/>\n" +
                "    <charm value=\"2LhSRXNAFG\"/>\n" +
                "    <birth value=\"1963-02-16\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "  </client>\n" +
                "  <client id=\"1-X6T-EO-DQ-B98lOXRivx\"> <!-- 5 -->\n" +
                "    <mobilePhone>+7-351-585-50-26</mobilePhone>\n" +
                "    <name value=\"bMCDYgPhoP\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"swxgtZeJXTKfAJpBprGW\" house=\"6B\" flat=\"kH\"/>\n" +
                "      <register street=\"LlCfnjNk9pvFq7Y7oUWC\" house=\"qN\" flat=\"9K\"/>\n" +
                "    </address>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <patronymic value=\"bgSlrSgXTBhKq\"/>\n" +
                "    <homePhone>+7-050-865-57-42</homePhone>\n" +
                "    <birth value=\"1973-10-26\"/>\n" +
                "    <charm value=\"iOPXrCaH1L\"/>\n" +
                "    <surname value=\"9Pwoz4hddL\"/>\n" +
                "    <mobilePhone>+7-266-894-04-06</mobilePhone>\n" +
                "  </client>\n" +
                "  <client id=\"1-5QB-P5-ZN-d64Txb19lO\"> <!-- 6 -->\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <workPhone>+7-507-492-66-60</workPhone>\n" +
                "    <homePhone>+7-771-134-42-69</homePhone>\n" +
                "    <homePhone>+7-669-568-83-39</homePhone>\n" +
                "    <patronymic value=\"t4CXdCRjmp4iN\"/>\n" +
                "    <birth value=\"1993-01-24\"/>\n" +
                "    <name value=\"gCSvNfbyOQ\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"ZJsrK6NcQQ2SK8Y7T7pY\" house=\"jb\" flat=\"0v\"/>\n" +
                "      <register street=\"98Hmh4wOyyhK5rv1YwC4\" house=\"5A\" flat=\"j6\"/>\n" +
                "    </address>\n" +
                "    <mobilePhone>+7-201-835-87-30</mobilePhone>\n" +
                "    <charm value=\"L5JU9GWEr6\"/>\n" +
                "    <homePhone>+7-796-595-84-14</homePhone>\n" +
                "    <surname value=\"r46jJLkyYs\"/>\n" +
                "  </client>\n" +
                "  <client id=\"1-5QB-P5-ZN-d64Txb19lO\"> <!-- 7 -->\n" +
                "    <homePhone>+7-936-082-61-43</homePhone>\n" +
                "    <workPhone>+7-244-978-70-66</workPhone>\n" +
                "    <birth value=\"1932-05-06\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"NP8TeTUnvyLsLOxynNfH\" house=\"tL\" flat=\"3b\"/>\n" +
                "      <register street=\"0QzUiRLn1ZOMLPY1VZ4c\" house=\"XL\" flat=\"CM\"/>\n" +
                "    </address>\n" +
                "    <surname value=\"64VfFzTbfY\"/>\n" +
                "    <charm value=\"o9tOWMJxma\"/>\n" +
                "    <homePhone>+7-414-748-39-07</homePhone>\n" +
                "    <homePhone>+7-607-661-67-45</homePhone>\n" +
                "    <patronymic value=\"YnNOQfUea19mg\"/>\n" +
                "    <homePhone>+7-759-952-41-79</homePhone>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <workPhone>+7-019-184-83-14</workPhone>\n" +
                "    <name value=\"EnBafzucAC\"/>\n" +
                "  </client>\n" +
                "  <client id=\"4-8HX-MW-56-xvlY2NzqoN\"> <!-- 8 -->\n" +
                "    <mobilePhone>+7-908-457-86-27</mobilePhone>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"JfQDcvFC47aFmxnbUMGA\" house=\"4t\" flat=\"ds\"/>\n" +
                "      <register street=\"hTEHRZgMFhIFg5eo9l0T\" house=\"R4\" flat=\"4x\"/>\n" +
                "    </address>\n" +
                "    <name value=\"riBlyYrgPQ\"/>\n" +
                "    <workPhone>+7-184-841-11-97</workPhone>\n" +
                "    <charm value=\"XSp0wmPiYd\"/>\n" +
                "    <workPhone>+7-944-149-78-31</workPhone>\n" +
                "    <surname value=\"JpTy45OfBa\"/>\n" +
                "    <birth value=\"1974-10-25\"/>\n" +
                "    <workPhone>+7-141-909-83-69</workPhone>\n" +
                "  </client>\n" +
                "  <client id=\"7-Y7H-26-CI-rLfpdWEkXJ\"> <!-- 9 -->\n" +
                "    <surname value=\"DjoWxtvHXn\"/>\n" +
                "    <homePhone>+7-365-209-68-06</homePhone>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <workPhone>+7-764-744-35-78</workPhone>\n" +
                "    <homePhone>+7-630-277-15-83 вн. cBji</homePhone>\n" +
                "    <mobilePhone>+7-635-910-30-52</mobilePhone>\n" +
                "    <charm value=\"digcxj3yRv\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"cu6V3h5KLtsYIhpfZ2aL\" house=\"Jz\" flat=\"Ev\"/>\n" +
                "      <register street=\"iqaU35lyYuIEPsGIyBmB\" house=\"8b\" flat=\"gM\"/>\n" +
                "    </address>\n" +
                "    <name value=\"O2D5znPC11\"/>\n" +
                "    <patronymic value=\"  \"/>\n" +
                "    <birth value=\"1996-07-25\"/>\n" +
                "  </client>\n" +
                "  <client id=\"5-KPK-M8-5W-iDndm72i8i\"> <!-- 10 -->\n" +
                "    <name value=\"XCTPWdNzK8\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <mobilePhone>+7-458-767-68-96 вн. QMQo</mobilePhone>\n" +
                "    <charm value=\"L9cUbBdV3U\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"03EXU12FeYexczd84dFB\" house=\"dH\" flat=\"dn\"/>\n" +
                "      <register street=\"TzjzfbNuiOKZc2cgliLf\" house=\"L4\" flat=\"qL\"/>\n" +
                "    </address>\n" +
                "    <homePhone>+7-074-527-75-25</homePhone>\n" +
                "    <surname value=\"rhpLoUk8sL\"/>\n" +
                "    <birth value=\"1969-01-25\"/>\n" +
                "    <patronymic value=\"\"/>\n" +
                "  </client>\n" +
                "  <client id=\"6-NAP-6B-9O-lHM5dOxoHS\"> <!-- 11 -->\n" +
                "    <address>\n" +
                "      <fact street=\"24cdTtune5Kfmz6PUcHi\" house=\"zJ\" flat=\"0o\"/>\n" +
                "      <register street=\"sqzzoNeer4tUOI7CWKw6\" house=\"ZN\" flat=\"k4\"/>\n" +
                "    </address>\n" +
                "    <charm value=\"XSp0wmPiYd\"/>\n" +
                "    <name value=\"9AR1pAGHEJ\"/>\n" +
                "    <mobilePhone>+7-350-045-72-32</mobilePhone>\n" +
                "    <workPhone>+7-589-419-69-57</workPhone>\n" +
                "    <birth value=\"2005-07-02\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <patronymic value=\"PHkCq9bGKZO8T\"/>\n" +
                "    <surname value=\"07k1oCyZEc\"/>\n" +
                "  </client>\n" +
                "  <client id=\"6-PXQ-2C-5V-V9SmbcbIlw\"> <!-- 12 -->\n" +
                "    <charm value=\"V5ltalM5ro\"/>\n" +
                "    <homePhone>+7-915-890-42-24 вн. W7hk</homePhone>\n" +
                "    <workPhone>+7-515-164-03-59 вн. IceV</workPhone>\n" +
                "    <address>\n" +
                "      <fact street=\"Jl6WFiw9FUrLg5yuevsV\" house=\"Gv\" flat=\"hq\"/>\n" +
                "      <register street=\"sfmkN6WCtJtaSwIeDk6l\" house=\"rH\" flat=\"c6\"/>\n" +
                "    </address>\n" +
                "    <surname value=\"qfOdWeHKi3\"/>\n" +
                "    <patronymic value=\"CEbUtlTaNrr33\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <homePhone>+7-069-473-74-08</homePhone>\n" +
                "    <name value=\"zekUfH01GM\"/>\n" +
                "    <birth value=\"1942-06-06\"/>\n" +
                "  </client>\n" +
                "  <client id=\"2-LLT-ZT-M3-SR1Pa6K8p9\"> <!-- 13 -->\n" +
                "    <mobilePhone>+7-467-965-43-30</mobilePhone>\n" +
                "    <workPhone>+7-169-146-68-11 вн. K65i</workPhone>\n" +
                "    <mobilePhone>+7-639-331-89-79</mobilePhone>\n" +
                "    <mobilePhone>+7-074-441-24-45</mobilePhone>\n" +
                "    <charm value=\"YQDeUF68V1\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"KTkTeyRIacNfDADxZt7C\" house=\"b2\" flat=\"NR\"/>\n" +
                "      <register street=\"auI0fUAYv03jW3xuOioR\" house=\"kz\" flat=\"Ig\"/>\n" +
                "    </address>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <patronymic value=\"VEIY0Fm1WMU3R\"/>\n" +
                "    <birth value=\"1980-12-15\"/>\n" +
                "    <name value=\"55XP9gsF8Z\"/>\n" +
                "    <workPhone>+7-661-665-31-47</workPhone>\n" +
                "    <homePhone>+7-979-061-66-50</homePhone>\n" +
                "    <surname value=\"ecjrveQ2HE\"/>\n" +
                "  </client>\n" +
                "  <client id=\"6-NAP-6B-9O-lHM5dOxoHS\"> <!-- 14 -->\n" +
                "    <surname value=\"NAOevzrb8b\"/>\n" +
                "    <patronymic value=\"\"/>\n" +
                "    <charm value=\"7wKHfMhQdr\"/>\n" +
                "    <workPhone>+7-764-002-96-27</workPhone>\n" +
                "    <mobilePhone>+7-963-952-38-52</mobilePhone>\n" +
                "    <name value=\"UBhfGDXDks\"/>\n" +
                "    <birth value=\"1970-12-24\"/>\n" +
                "    <workPhone>+7-707-979-68-26 вн. wfdN</workPhone>\n" +
                "    <address>\n" +
                "      <fact street=\"qXcBn1Sl0F0CKHZSbvTP\" house=\"W1\" flat=\"Rj\"/>\n" +
                "      <register street=\"F213i8c5gP7UFjudCK2d\" house=\"nq\" flat=\"d1\"/>\n" +
                "    </address>\n" +
                "    <workPhone>+7-411-829-79-86</workPhone>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "  </client>\n" +
                "  <client id=\"4-KQP-HH-EG-PBwRFMNqlK\"> <!-- 15 -->\n" +
                "    <patronymic value=\"UGzZigLqOIGdT\"/>\n" +
                "    <mobilePhone>+7-672-019-78-40 вн. o8yq</mobilePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"oUyH2JuqZeRqqVwYDL4O\" house=\"fL\" flat=\"Bf\"/>\n" +
                "      <register street=\"nzradXeg4jPLXfgq6vyu\" house=\"CB\" flat=\"WU\"/>\n" +
                "    </address>\n" +
                "    <charm value=\"AfmXqdNJIg\"/>\n" +
                "    <homePhone>+7-065-273-42-40</homePhone>\n" +
                "    <surname value=\"peijxjFuLN\"/>\n" +
                "    <birth value=\"1979-01-14\"/>\n" +
                "    <homePhone>+7-207-030-59-69</homePhone>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <name value=\"DEySsyOJPk\"/>\n" +
                "  </client>\n" +
                "  <client id=\"6-NAP-6B-9O-lHM5dOxoHS\"> <!-- 16 -->\n" +
                "    <surname value=\"KPK87g7gjB\"/>\n" +
                "    <homePhone>+7-272-546-67-19</homePhone>\n" +
                "    <mobilePhone>+7-008-812-75-50</mobilePhone>\n" +
                "    <birth value=\"1938-12-01\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <workPhone>+7-689-392-83-79</workPhone>\n" +
                "    <address>\n" +
                "      <fact street=\"lCDhbY21v1ytorATR1sz\" house=\"68\" flat=\"Lm\"/>\n" +
                "      <register street=\"mvBsD73Pb2vqOHe165u6\" house=\"fO\" flat=\"Ie\"/>\n" +
                "    </address>\n" +
                "    <patronymic value=\"76vVzUZOz30wU\"/>\n" +
                "    <charm value=\"EfhISO7BqR\"/>\n" +
                "    <mobilePhone>+7-656-237-62-43</mobilePhone>\n" +
                "    <name value=\"MAQY5lMByD\"/>\n" +
                "    <mobilePhone>+7-677-721-26-24</mobilePhone>\n" +
                "    <mobilePhone>+7-544-917-76-40</mobilePhone>\n" +
                "  </client>\n" +
                "  <client id=\"6-PXQ-2C-5V-V9SmbcbIlw\"> <!-- 17 -->\n" +
                "    <surname value=\"hze8K8OWBk\"/>\n" +
                "    <homePhone>+7-620-775-58-58</homePhone>\n" +
                "    <name value=\"WY7qVdc4MU\"/>\n" +
                "    <patronymic value=\"Evy9vy3X8drV4\"/>\n" +
                "    <mobilePhone>+7-897-253-79-20</mobilePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"LrKq25s30RuO2CwaoRk4\" house=\"h8\" flat=\"qV\"/>\n" +
                "      <register street=\"Sgkukt6KWGKHbTKvtL6m\" house=\"LA\" flat=\"rN\"/>\n" +
                "    </address>\n" +
                "    <workPhone>+7-111-538-00-71</workPhone>\n" +
                "    <birth value=\"1957-09-25\"/>\n" +
                "    <charm value=\"AiPWGsdxL8\"/>\n" +
                "    <homePhone>+7-438-435-49-39</homePhone>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <workPhone>+7-932-916-22-55</workPhone>\n" +
                "  </client>\n" +
                "  <client id=\"5-KPK-M8-5W-iDndm72i8i\"> <!-- 18 -->\n" +
                "    <workPhone>+7-415-781-47-39</workPhone>\n" +
                "    <address>\n" +
                "      <fact street=\"Q05FNE2XSOcbI3Q0gnkg\" house=\"JJ\" flat=\"ui\"/>\n" +
                "      <register street=\"wBnrMIzZTEwEpDYiwW0y\" house=\"6q\" flat=\"O3\"/>\n" +
                "    </address>\n" +
                "    <surname value=\"UHgkDmjWJM\"/>\n" +
                "    <charm value=\"jxQ6VjyHdT\"/>\n" +
                "    <birth value=\"1954-01-23\"/>\n" +
                "    <patronymic value=\"K2VujbweDri2y\"/>\n" +
                "    <name value=\"2FOehwwFzi\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <mobilePhone>+7-958-890-98-15</mobilePhone>\n" +
                "    <homePhone>+7-007-998-67-47</homePhone>\n" +
                "  </client>\n" +
                "  <client id=\"3-3X0-G5-YH-CbETEfgBc9\"> <!-- 19 -->\n" +
                "    <birth value=\"1947-02-01\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <workPhone>+7-245-496-85-55 вн. KOwC</workPhone>\n" +
                "    <surname value=\"AgzFE43uvu\"/>\n" +
                "    <mobilePhone>+7-140-368-78-21</mobilePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"y0MqVZkNCiZBQNH43ZdL\" house=\"Vx\" flat=\"Oj\"/>\n" +
                "      <register street=\"sZgHv4Oz4QDdoiq16MhM\" house=\"cF\" flat=\"m8\"/>\n" +
                "    </address>\n" +
                "    <mobilePhone>+7-249-650-67-03</mobilePhone>\n" +
                "    <mobilePhone>+7-504-673-79-33</mobilePhone>\n" +
                "    <patronymic value=\"\"/>\n" +
                "    <name value=\"LkXAMXgg6d\"/>\n" +
                "    <charm value=\"irtkeuuVRH\"/>\n" +
                "  </client>\n" +
                "  <client id=\"1-27H-7U-R1-26nFc7H8Bs\"> <!-- 20 -->\n" +
                "    <workPhone>+7-177-802-84-07</workPhone>\n" +
                "    <charm value=\"EfhISO7BqR\"/>\n" +
                "    <patronymic value=\"mHGJbClWEg3Bt\"/>\n" +
                "    <birth value=\"1529-12-03\"/>\n" +
                "    <workPhone>+7-156-338-14-13 вн. CzEm</workPhone>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <homePhone>+7-415-319-87-39 вн. c7Sh</homePhone>\n" +
                "    <surname value=\"fUZ5TqBWnm\"/>\n" +
                "    <name value=\"U17QD7MDv8\"/>\n" +
                "    <mobilePhone>+7-469-623-52-63</mobilePhone>\n" +
                "    <homePhone>+7-401-094-66-08</homePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"SG0OY1uhYqHeSqTIuulC\" house=\"XK\" flat=\"Xq\"/>\n" +
                "      <register street=\"VmI3Ng5Ayzqa7veNQNnp\" house=\"Sc\" flat=\"bm\"/>\n" +
                "    </address>\n" +
                "    <mobilePhone>+7-198-038-93-88</mobilePhone>\n" +
                "  </client>\n" +
                "  <client id=\"2-WAH-YO-EL-nmLiOVEQmS\"> <!-- 21 -->\n" +
                "    <patronymic value=\"  \"/>\n" +
                "    <mobilePhone>+7-866-795-81-82 вн. aDqg</mobilePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"pfmXCvursmGa8lzGYJXM\" house=\"dj\" flat=\"nx\"/>\n" +
                "      <register street=\"FdOHZYrppggtg9Y0kKDD\" house=\"cc\" flat=\"Dy\"/>\n" +
                "    </address>\n" +
                "    <mobilePhone>+7-895-487-99-41</mobilePhone>\n" +
                "    <name value=\"IzBJqUbSeA\"/>\n" +
                "    <mobilePhone>+7-544-865-07-02</mobilePhone>\n" +
                "    <mobilePhone>+7-044-850-53-85</mobilePhone>\n" +
                "    <surname value=\"Vwf3BCGLC4\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <charm value=\"2LhSRXNAFG\"/>\n" +
                "    <birth value=\"1961-04-14\"/>\n" +
                "  </client>\n" +
                "  <client id=\"6-NAP-6B-9O-lHM5dOxoHS\"> <!-- 22 -->\n" +
                "    <birth value=\"1995-05-22\"/>\n" +
                "    <patronymic value=\"pDG5mums6HYVr\"/>\n" +
                "    <name value=\"lyuA2clv1K\"/>\n" +
                "    <homePhone>+7-761-255-47-79</homePhone>\n" +
                "    <surname value=\"2gAEUoshTq\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <charm value=\"a1yZeVbPmK\"/>\n" +
                "    <mobilePhone>+7-692-266-70-05 вн. XuWi</mobilePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"dL6WQ7JS2iKc3OoYAebn\" house=\"VH\" flat=\"7T\"/>\n" +
                "      <register street=\"JPEJLCTFOR5BpPgWnpVR\" house=\"NS\" flat=\"Pd\"/>\n" +
                "    </address>\n" +
                "  </client>\n" +
                "  <client id=\"6-PXQ-2C-5V-V9SmbcbIlw\"> <!-- 23 -->\n" +
                "    <mobilePhone>+7-926-980-63-06</mobilePhone>\n" +
                "    <name value=\"zQXsolZbut\"/>\n" +
                "    <patronymic value=\"ULL3KeOMIejn4\"/>\n" +
                "    <workPhone>+7-371-323-47-50</workPhone>\n" +
                "    <charm value=\"ZMnRRYVEHK\"/>\n" +
                "    <surname value=\"Yl6gyzI0D8\"/>\n" +
                "    <homePhone>+7-693-201-74-33</homePhone>\n" +
                "    <birth value=\"1927-07-21\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"oZvKc42FcoskqTxax8Qb\" house=\"BM\" flat=\"cc\"/>\n" +
                "      <register street=\"97WEVGnu978mW8ZqUcpB\" house=\"mI\" flat=\"6F\"/>\n" +
                "    </address>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "  </client>\n" +
                "  <client id=\"5-KPK-M8-5W-iDndm72i8i\"> <!-- 24 -->\n" +
                "    <homePhone>+7-131-556-13-48 вн. BgG8</homePhone>\n" +
                "    <homePhone>+7-625-642-03-00</homePhone>\n" +
                "    <charm value=\"FE0ItrUK1z\"/>\n" +
                "    <birth value=\"1959-11-27\"/>\n" +
                "    <surname value=\"gTIGpyR8K7\"/>\n" +
                "    <name value=\"Ck0tGrA6mq\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"uXaT6xWNty1piqArtcBu\" house=\"w9\" flat=\"NQ\"/>\n" +
                "      <register street=\"726OK1cJWIFcCZlmRS7p\" house=\"oB\" flat=\"Dq\"/>\n" +
                "    </address>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <workPhone>+7-662-919-19-56</workPhone>\n" +
                "    <patronymic value=\"GsPsmtJp8rYbE\"/>\n" +
                "    <mobilePhone>+7-199-218-88-28</mobilePhone>\n" +
                "    <homePhone>+7-946-093-08-41</homePhone>\n" +
                "  </client>\n" +
                "  <client id=\"9-JIU-ZY-A1-A91mSIR5xa\"> <!-- 25 -->\n" +
                "    <birth value=\"1946-07-20\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"xrygdmSjKfcQVYng10eP\" house=\"Mj\" flat=\"Hz\"/>\n" +
                "      <register street=\"iVeOZ1XiniKwWtI5LbCL\" house=\"5g\" flat=\"7M\"/>\n" +
                "    </address>\n" +
                "    <charm value=\"YQDeUF68V1\"/>\n" +
                "    <name value=\"0ttWhnm03M\"/>\n" +
                "    <homePhone>+7-845-795-89-15</homePhone>\n" +
                "    <mobilePhone>+7-749-883-27-89</mobilePhone>\n" +
                "    <surname value=\"dd2EDF49Eu\"/>\n" +
                "    <mobilePhone>+7-106-690-11-45</mobilePhone>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <patronymic value=\"gtOA7koRJqtfF\"/>\n" +
                "  </client>\n" +
                "  <client id=\"1-X6T-EO-DQ-B98lOXRivx\"> <!-- 26 -->\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <workPhone>+7-171-976-89-66</workPhone>\n" +
                "    <charm value=\"jxQ6VjyHdT\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"9r9jdIyZfMACPCmzPANn\" house=\"pG\" flat=\"wG\"/>\n" +
                "      <register street=\"iRBMD5VhtiKziQrtcPUx\" house=\"A7\" flat=\"2W\"/>\n" +
                "    </address>\n" +
                "    <mobilePhone>+7-074-188-10-27</mobilePhone>\n" +
                "    <patronymic value=\"k1SM1rVAPpg8B\"/>\n" +
                "    <name value=\"9MMxfDO3Tx\"/>\n" +
                "    <mobilePhone>+7-654-374-23-10 вн. JbLn</mobilePhone>\n" +
                "    <mobilePhone>+7-454-391-22-43 вн. jMQm</mobilePhone>\n" +
                "    <homePhone>+7-635-507-24-08 вн. ujsl</homePhone>\n" +
                "    <surname value=\"o5uR2GmeNt\"/>\n" +
                "    <birth value=\"1967-03-21\"/>\n" +
                "  </client>\n" +
                "  <client id=\"3-7QE-YD-5H-nLcI1yJYdK\"> <!-- 27 -->\n" +
                "    <patronymic value=\"jtoqnty3Sk24I\"/>\n" +
                "    <homePhone>+7-230-896-64-19</homePhone>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"zijNhWSETpyhn0z2Vldx\" house=\"wK\" flat=\"xR\"/>\n" +
                "      <register street=\"J7qjvzwjhojHY8fTSFBJ\" house=\"ZM\" flat=\"Qm\"/>\n" +
                "    </address>\n" +
                "    <charm value=\"YQDeUF68V1\"/>\n" +
                "    <name value=\"TsSImnjyF4\"/>\n" +
                "    <workPhone>+7-595-172-90-71 вн. HQND</workPhone>\n" +
                "    <surname value=\"GmT5Oh73NJ\"/>\n" +
                "    <birth value=\"1953-04-15\"/>\n" +
                "  </client>\n" +
                "  <client id=\"0-IG7-E4-MW-5gTAi78Lqw\"> <!-- 28 -->\n" +
                "    <patronymic value=\"  \"/>\n" +
                "    <mobilePhone>+7-526-959-86-64</mobilePhone>\n" +
                "    <address>\n" +
                "      <fact street=\"F7lKAzEYpXLZUAO0LgdO\" house=\"ZO\" flat=\"s0\"/>\n" +
                "      <register street=\"j8cYU9Pl1apD9R0UCemX\" house=\"kL\" flat=\"6g\"/>\n" +
                "    </address>\n" +
                "    <mobilePhone>+7-579-585-68-32</mobilePhone>\n" +
                "    <birth value=\"1925-07-09\"/>\n" +
                "    <name value=\"E50ZmzZ2Mq\"/>\n" +
                "    <homePhone>+7-603-460-17-07</homePhone>\n" +
                "    <workPhone>+7-225-178-94-58</workPhone>\n" +
                "    <charm value=\"L5JU9GWEr6\"/>\n" +
                "    <homePhone>+7-903-428-98-47</homePhone>\n" +
                "    <surname value=\"JZZoYu6QIn\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <mobilePhone>+7-869-782-67-91</mobilePhone>\n" +
                "  </client>\n" +
                "  <client id=\"1-X6T-EO-DQ-B98lOXRivx\"> <!-- 29 -->\n" +
                "    <patronymic value=\"6JTnHyi2lvGfi\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"asnicmPEYSJHjKH0qG1a\" house=\"W9\" flat=\"sQ\"/>\n" +
                "      <register street=\"vBxtr188xTtlA4WDVQjR\" house=\"9t\" flat=\"2k\"/>\n" +
                "    </address>\n" +
                "    <workPhone>+7-689-104-55-02</workPhone>\n" +
                "    <charm value=\"BNySAe83tg\"/>\n" +
                "    <birth value=\"1941-11-06\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <workPhone>+7-240-221-48-55</workPhone>\n" +
                "    <name value=\"NLkopEQeUH\"/>\n" +
                "    <surname value=\"O3IIVD4wYH\"/>\n" +
                "  </client>\n" +
                "  <client id=\"4-KQP-HH-EG-PBwRFMNqlK\"> <!-- 30 -->\n" +
                "    <charm value=\"zNeTrsQgAL\"/>\n" +
                "    <mobilePhone>+7-750-524-63-86</mobilePhone>\n" +
                "    <workPhone>+7-881-851-28-74 вн. 5Pfn</workPhone>\n" +
                "    <birth value=\"1990-05-20\"/>\n" +
                "    <patronymic value=\"  \"/>\n" +
                "    <surname value=\"qtBRqAFvfm\"/>\n" +
                "    <name value=\"EsXGujNSAc\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"KkrgoF6eJlD1AJlc2h7v\" house=\"Lt\" flat=\"dO\"/>\n" +
                "      <register street=\"S79vfJY9NMIaTbC2HiWC\" house=\"6P\" flat=\"5O\"/>\n" +
                "    </address>\n" +
                "  </client>\n" +
                "</cia>\n" +
                "\n";

        String directoryPath = "kz/greetgo/sandboxserver/migration/testXml100/";
        String fileName = "cia_test_100.xml";
        String filePath = directoryPath + fileName;

        // Create the directory if it does not exist
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directoryPath);
            } else {
                System.err.println("Failed to create the directory.");
                return;
            }
        }
        try {
            File file = new File(filePath);
            FileWriter writer = new FileWriter(file);
            writer.write(xmlData);
            writer.flush();
            writer.close();
            System.out.println("XML file generated successfully: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String directoryPath2 = "kz/greetgo/sandboxserver/migration/testXml100/";
        File outputDirectory = new File(directoryPath2);
        File[] xmlFiles = outputDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (xmlFiles == null) {
            System.out.println("No XML files found in the directory.");
            return;
        }

        String insertClientSQL = "INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO phone_tmp (client_id, type, number) VALUES (?,?,?)";
        try (Connection connection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp")) {
            assert connection != null;
            try (PreparedStatement ciaPS = connection.prepareStatement(insertClientSQL);
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
                    assertThat(count).isEqualTo(30);
                }
                String sql2 = "SELECT count(*) FROM phone_tmp";
                ResultSet resultSet1 = statement.executeQuery(sql2);
                if (resultSet1.next()) {
                    int count = resultSet1.getInt(1);
                    assertThat(count).isEqualTo(101);
                }

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
            if (resultSet.next()) assertThat(resultSet.getInt(1)).isEqualTo(6);

            ResultSet resultSet2 = statement.executeQuery("SELECT count(*) FROM client_phone");
            if (resultSet2.next()) assertThat(resultSet2.getInt(1)).isEqualTo(20);

            ResultSet resultSet3 = statement.executeQuery("SELECT count(*) FROM client_addr");
            if (resultSet3.next()) assertThat(resultSet3.getInt(1)).isEqualTo(22);

            ResultSet resultSet4 = statement.executeQuery("SELECT count(*) FROM client");
            if (resultSet4.next()) assertThat(resultSet4.getInt(1)).isEqualTo(11);

        }
    }

}
