package kz.greetgo.sandboxserver.migration;

import kz.greetgo.sandboxserver.DatabaseAccess;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CiaMigrationTest {
    private Connection connection;
    private CiaMigration migration;
    private DatabaseAccess dbAccess;

    @BeforeTest
    public void createTables() {
        DatabaseSetup.createActualTables();
    }

    @BeforeMethod
    public void setUp() throws SQLException {
        connection = DatabaseSetup.getConnection();
        migration = new CiaMigration(connection);
        migration.exec("DELETE FROM client_addr WHERE client = 'id_003-check'");
        migration.exec("DELETE FROM client_phone WHERE client = 'id_003-check'");
        migration.exec("DELETE FROM client WHERE id = 'id_003-check'");
        migration.exec("DELETE FROM charm WHERE name = 'unique_charm'");
        migration.prepareStorages();

        dbAccess = new DatabaseAccess(connection);
    }

    @AfterMethod
    public void tearDown() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
            if (migration != null) {
                migration.close();
                migration = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void assertClientsHaveError(String error, String tmpClientTable) throws SQLException {
        boolean foundClient = false;

        try (Statement statement = connection.createStatement()) {
            //language=PostgresSQL
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM " + tmpClientTable);
            while (resultSetC.next()) {
                foundClient = true;

                String status = resultSetC.getString("status");
                String errorMsg = resultSetC.getString("error");

                assertThat(status).isEqualTo("ERROR");
                assertThat(errorMsg).isEqualTo(error);
            }
        }
        if (!foundClient) {
            throw new AssertionError("No client found in " + tmpClientTable);
        }
    }

    private void assertClientsHaveNoError(String tmpClientTable) throws SQLException {
        boolean foundClient = false;

        try (Statement statement = connection.createStatement()) {
            //language=PostgresSQL
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM " + tmpClientTable);
            while (resultSetC.next()) {
                foundClient = true;

                String status = resultSetC.getString("status");
                String errorMsg = resultSetC.getString("error");

                assertThat(status).isEqualTo("JUST INSERTED");
                assertThat(errorMsg).isEqualTo("");
            }
        }
        if (!foundClient) {
            throw new AssertionError("No client found in " + tmpClientTable);
        }
    }


    private void assertPhoneExists(String phoneNumber, String type) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + migration.tmpPhoneTable + " WHERE type = '" + type + "' AND number = '" + phoneNumber + "'")) {
            if (resultSet.next()) {
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            } else {
                throw new AssertionError("Phone with type " + type + " and number " + phoneNumber + " not found in " + migration.tmpClientTable);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void assertPhoneExists(String phoneNumber, String type, String client_id) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM client_phone WHERE client = '" + client_id + "' AND type = '" + type + "' AND number = '" + phoneNumber + "'")) {
            if (resultSet.next()) {
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            } else {
                throw new AssertionError("Phone with client_id: " + client_id + ", type: + " + type + ", number: " + phoneNumber + " not found in client_phone table");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void insertClientToTmp(Client client) throws SQLException {
        migration.exec(
                String.format("INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, " +
                                "fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                        client.id, client.surname, client.name, client.patronymic, client.gender, client.charm,
                        client.birth, client.factStreet, client.factHouse, client.factFlat, client.registerStreet,
                        client.registerHouse, client.registerFlat, client.error, client.status));
    }

    @Test
    public void validateSurnameAbsence___ShouldFlagClientAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "''", // No Surname
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2000-2-31'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateSurnameAbsence();
        //
        //

        assertClientsHaveError("surname is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateSurnameAbsence___Should_Not_FlagClientAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'", // Surname
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2000-2-31'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateSurnameAbsence();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateNameAbsence___ShouldFlagClientAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "''", // No Name
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2000-2-31'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateNameAbsence();
        //
        //

        assertClientsHaveError("name is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateNameAbsence___Should_Not_FlagClientAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'", // Name
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2000-2-31'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateNameAbsence();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateBirthDateAbsence___ShouldFlagClientAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "''", // No Birth
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateBirthDateAbsence();
        //
        //

        assertClientsHaveError("birth_date is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateBirthDateAbsence___Should_Not_FlagClientAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2000-12-12'", // Birth
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateBirthDateAbsence();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateBirthDatePattern___ShouldFlagClientsAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'12-12-2002'", // Not Correct Pattern (YYYY-MM-DD)
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2002-02-30'", // February only 28 days
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateBirthDatePattern();
        //
        //

        assertClientsHaveError("birth_date is not correct", migration.tmpClientTable);
    }

    @Test
    public void validateBirthDatePattern___Should_Not_FlagClientsAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2002-12-12'", // Correct Pattern (YYYY-MM-DD)
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2002-02-28'", // Correct Pattern (YYYY-MM-DD)
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateBirthDatePattern();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateAgeRange___ShouldFlagClientsAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2022-12-30'", // less than 18 y.o
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'1901-12-30'", // more than 100 y.o
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateAgeRange();
        //
        //

        assertClientsHaveError("AGE_OUT_OF_RANGE", migration.tmpClientTable);
    }

    @Test
    public void validateAgeRange___Should_Not_FlagClientsAsError() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2005-01-01'", // 18 y.o
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.validateAgeRange();
        //
        //

        assertClientsHaveError("AGE_OUT_OF_RANGE", migration.tmpClientTable);
    }


    @Test
    public void markDuplicateClients___ShouldMark_FirstClientAsDuplicate() throws SQLException {
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name1'", // Clients added earlier will be marked as duplicates
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2022-12-30'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));
        insertClientToTmp(new Client(
                "'id_001'",
                "'surname'",
                "'name2'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2022-12-30'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.markDuplicateClients();
        //
        //

        String tableName = migration.tmpClientTable;
        String condition = "status = 'DUPLICATE'";

        int id = dbAccess.getRowCountFromTableWithCondition(tableName, condition);
        assertThat(id).isEqualTo(1);

        Map<String, String> map = dbAccess.getClientFromTableWithCondition(tableName, condition);
        assertThat(map.get("name")).isEqualTo("name1");
    }

    @Test
    public void markDuplicatePhones___ShouldMark_FirstSamePhonesAsDuplicate() throws SQLException {
        migration.prepareStorages();
        migration.exec("INSERT INTO TMP_PHONE (client_id, type, number, status) VALUES " +
                "('id_001', 'HOME', '+7-718-096-63-80 вн. Y2RH','JUST INSERTED'), " +
                "('id_001', 'HOME', '+7-718-096-63-80 вн. Y2RH','JUST INSERTED'), " +
                "('id_001', 'WORK', '+888080130123','JUST INSERTED'), " +
                "('id_001', 'WORK', '+888080130123','JUST INSERTED'), " +
                "('id_001', 'MOBILE', '+123123123123','JUST INSERTED'), " +
                "('id_001', 'MOBILE', '+123123123123','JUST INSERTED')");

        //
        //
        migration.markDuplicatePhones();
        //
        //

        String tableName = migration.tmpPhoneTable;

        int duplicateCount = dbAccess.getRowCountFromTableWithCondition(tableName, "status = 'DUPLICATE'");

        assertThat(duplicateCount).isEqualTo(3);

        Map<String, String> map = dbAccess.getPhoneFromTableWithCondition(tableName, "type = 'HOME' AND status='DUPLICATE'");

        assertThat(map.get("id")).isEqualTo("1");
        assertThat(map.get("number")).isEqualTo("+7-718-096-63-80 вн. Y2RH");

        Map<String, String> map2 = dbAccess.getPhoneFromTableWithCondition(tableName, "type = 'WORK' AND status='DUPLICATE'");

        assertThat(map2.get("id")).isEqualTo("3");
        assertThat(map2.get("number")).isEqualTo("+888080130123");

        Map<String, String> map3 = dbAccess.getPhoneFromTableWithCondition(tableName, "type = 'MOBILE' AND status='DUPLICATE'");

        assertThat(map3.get("id")).isEqualTo("5");
        assertThat(map3.get("number")).isEqualTo("+123123123123");
    }

    @Test
    public void uploadToTmp___ShouldCheckIfClientUploadedCorrectly() throws SQLException {
        String str = "" +
                "<cia>" +
                "  <client id=\"id_003-check\">" +
                "    <surname value=\"snv435hv5\"/>" +
                "    <birth value=\"12-12-2002\"/>" +
                "    <name value=\"n\"/>" +
                "    <address>" +
                "      <fact street=\"1CbPis8PMcGfcBST4Q3zap\" house=\"s5\" flat=\"Di\"/>" +
                "      <register street=\"Fh5PGzqIoxXK6r8Jg4zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "    </address>" +
                "    <gender value=\"FEMALE\"/>" +
                "    <workPhone>+7-165-867-45-80</workPhone>" +
                "    <mobilePhone>+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "    <homePhone value=\"+7-718-096-63-20\"/>" +
                "    <charm value=\"4S7UG5gvo23k\"/>" +
                "    <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "  </client>" +
                "</cia>";

        migration.inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        migration.prepareStorages();

        //
        //
        migration.uploadToTmp();
        //
        //

        String tableName = migration.tmpClientTable;
        String condition = "client_id = 'id_003-check'";

        Map<String, String> map = dbAccess.getClientFromTableWithCondition(tableName, condition);

        assertThat(map.get("client_id")).isEqualTo("id_003-check");
        assertThat(map.get("surname")).isEqualTo("s");
        assertThat(map.get("birth")).isEqualTo("12-12-2002");
        assertThat(map.get("name")).isEqualTo("n");
        assertThat(map.get("fact_street")).isEqualTo("1CbPis8PMcGfcBST4Q3zap");
        assertThat(map.get("register_street")).isEqualTo("Fh5PGzqIoxXK6r8Jg4zqH");
        assertThat(map.get("fact_house")).isEqualTo("s5");
        assertThat(map.get("register_house")).isEqualTo("Jq");
        assertThat(map.get("fact_flat")).isEqualTo("Di");
        assertThat(map.get("register_flat")).isEqualTo("ta");
        assertThat(map.get("charm")).isEqualTo("4S7UG5gvo23k");
        assertThat(map.get("patronymic")).isEqualTo("sn7FcW6YHyhRo");
        assertThat(map.get("gender")).isEqualTo("FEMALE");
        assertPhoneExists("+7-165-867-45-80", "WORK");
        assertPhoneExists("+7-718-096-63-80 вн. Y2RH", "MOBILE");
        assertPhoneExists("+7-718-096-63-20", "HOME");
    }
    @Test()
    public void upsertDataToCharm___ShouldCheckIfCharmExists() throws SQLException {
        insertClientToTmp(new Client(
                "'id_003-check'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'unique_charm'",
                "'2000-01-29'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'FOR INSERT'"
        ));

        //
        //
        migration.upsertDataToCharm();
        //
        //

        int charmCount = dbAccess.getCharmCountByName("unique_charm");
        assertThat(charmCount).isEqualTo(1);
    }
    @Test()
    public void upsertDataToCharm___ShouldIgnoreDuplicateCharmName() throws SQLException {
        migration.prepareStorages();
        insertClientToTmp(new Client(
                "'id_003-check'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'unique_charm'",
                "'2000-01-29'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'FOR INSERT'"
        ));

        migration.upsertDataToCharm();

        insertClientToTmp(new Client(
                "'id_003-check'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'unique_charm'",
                "'2000-01-29'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'FOR INSERT'"
        ));

        //
        //
        migration.upsertDataToCharm();
        //
        //

        int charmCount = dbAccess.getCharmCountByName("unique_charm");
        assertThat(charmCount).isEqualTo(1);
    }
    @Test
    public void insertDataToClient___ShouldCheckIfClientExists() throws SQLException {
        insertClientToTmp(new Client(
                "'id_003-check'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2000-01-29'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'FOR INSERT'"
        ));
        migration.upsertDataToCharm();

        //
        //
        migration.insertDataToClient();
        //
        //

        Map<String, String> map = dbAccess.getClientById("id_003-check");

        assertThat(map.get("id")).isEqualTo("id_003-check");
        assertThat(map.get("surname")).isEqualTo("surname");
        assertThat(map.get("name")).isEqualTo("name");
        assertThat(map.get("patronymic")).isEqualTo("patronymic");
        assertThat(map.get("gender")).isEqualTo("MALE");
        assertThat(map.get("birth_date")).isEqualTo("2000-01-29");
        assertThat(map.get("charm")).isEqualTo("choleric");
    }
    @Test
    public void markUpdateAndInsertClients___ShouldMark_ClientAsForUpdate() throws SQLException {
        migration.prepareStorages();
        insertClientToTmp(new Client(
                "'id_003-check'", // Will be existing Client
                "'surname'",
                "'name1'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2022-12-30'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'FOR INSERT'"
        ));
        migration.insertDataToClient();

        migration = new CiaMigration(connection); // 2nd Migration
        migration.prepareStorages();

        insertClientToTmp(new Client(
                "'id_003-check'", // Will be Client For Update
                "'surname'",
                "'name2'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2022-12-30'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'JUST INSERTED'"
        ));

        //
        //
        migration.markUpdateAndInsertClients();
        //
        //

        String tableName = migration.tmpClientTable;
        String condition = "status = 'FOR UPDATE'";

        int id = dbAccess.getRowCountFromTableWithCondition(tableName, condition);
        assertThat(id).isEqualTo(1);

        Map<String, String> map = dbAccess.getClientFromTableWithCondition(tableName, condition);
        assertThat(map.get("name")).isEqualTo("name2");
    }

    @Test()
    public void updateDataInClient___ShouldCheckForUpdatesOfOldValues() throws SQLException {
        migration.prepareStorages();
        insertClientToTmp(new Client(
                "'id_003-check'", // Will be existing Client
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2000-12-30'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'FOR INSERT'"
        ));
        migration.upsertDataToCharm();
        migration.insertDataToClient();

        migration = new CiaMigration(connection); // 2nd Migration
        migration.prepareStorages();
        insertClientToTmp(new Client(
                "'id_003-check'", // Will be Client For Update
                "'new_surname'",
                "'new_name'",
                "'new_patronymic'",
                "'FEMALE'",
                "'choleric'",
                "'2002-01-30'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'Tole bi st.'",
                "'122A'",
                "'1st floor'",
                "''",
                "'FOR UPDATE'"
        ));

        //
        //
        migration.updateDataInClient();
        //
        //

        Map<String, String> map = dbAccess.getClientById("id_003-check");

        assertThat(map.get("id")).isEqualTo("id_003-check");
        assertThat(map.get("surname")).isEqualTo("new_surname");
        assertThat(map.get("name")).isEqualTo("new_name");
        assertThat(map.get("patronymic")).isEqualTo("new_patronymic");
        assertThat(map.get("birth_date")).isEqualTo("2002-01-30");
        assertThat(map.get("charm")).isEqualTo("choleric");
        assertThat(map.get("gender")).isEqualTo("FEMALE");
    }

    @Test()
    public void upsertDataToClientAddr___shouldUpsertNewAddressFromTmpClient() throws SQLException {
        Client clientToInsert = new Client(
                "'id_003-check'",
                "'surname'",
                "'name'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2022-12-30'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "'asd st.'",
                "'145B'",
                "'7th floor'",
                "''",
                "'FOR INSERT'"
        );
        Client clientToUpdate = new Client(
                "'id_003-check'",
                "'surname'",
                "'name1'",
                "'patronymic'",
                "'MALE'",
                "'choleric'",
                "'2022-12-30'",
                "''", // Should be empty
                "''",
                "''",
                "''",
                "''",
                "''",
                "''",
                "'FOR INSERT'"
        );

        migration.prepareStorages();
        insertClientToTmp(clientToInsert);
        migration.insertDataToClient();

        migration = new CiaMigration(connection);
        migration.prepareStorages();
        insertClientToTmp(clientToUpdate);

        //
        //
        migration.upsertDataToClientAddr();
        //
        //

        Map<String, String> reg = dbAccess.getClientAddrByIdAndType("id_003-check", "REG");
        Map<String, String> fact = dbAccess.getClientAddrByIdAndType("id_003-check", "FACT");

        assertThat(reg.get("street")).isEqualTo("");
        assertThat(reg.get("house")).isEqualTo("");
        assertThat(reg.get("flat")).isEqualTo("");
        assertThat(fact.get("street")).isEqualTo("");
        assertThat(fact.get("house")).isEqualTo("");
        assertThat(fact.get("flat")).isEqualTo("");
    }
    @Test
    public void upsertDataToClientPhone___ShouldUpsertPhonesFromTmpPhone() throws SQLException {
        String clientToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'unique_charm', '2000-1-29', " +
                "'asd st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String phonesToInsert = "INSERT INTO TMP_PHONE (client_id, type, number) VALUES " +
                "('id_003-check', 'HOME', '+7-718-096-63-80 вн. Y2RH'), " +
                "('id_003-check', 'WORK', '+7-708-304-22-23'), " +
                "('id_003-check', 'MOBILE', '+888080130123'), " +
                "('id_003-check', 'WORK', '+123123123123'), " +
                "('id_003-check', 'MOBILE', '+321321321')";

        migration.prepareStorages();
        migration.exec(clientToInsert);
        migration.exec(phonesToInsert);
        migration.insertDataToClient();

        //
        //
        migration.upsertDataToClientPhone();
        //
        //

        assertPhoneExists("+7-718-096-63-80 вн. Y2RH", "HOME", "id_003-check");
        assertPhoneExists("+321321321", "MOBILE", "id_003-check");
        assertPhoneExists("+888080130123", "MOBILE", "id_003-check");
        assertPhoneExists("+7-708-304-22-23", "WORK", "id_003-check");
        assertPhoneExists("+123123123123", "WORK", "id_003-check");
    }

    @Test
    public void uploadErrors___ShouldCheckErrorUploadAfterValidations() throws SQLException {
        String clientToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'unique_charm', '2000-1-29', " +
                "'asd st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String clientToInsertCopy = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'unique_charm', '2000-1-29', " +
                "'asd st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        migration.prepareStorages();
        migration.outputStream = outputErrors;
        migration.exec(clientToInsert);
        migration.exec(clientToInsertCopy);
        migration.markDuplicateClients();

        //
        //
        migration.uploadErrors();
        //
        //

        String errors = outputErrors.toString(StandardCharsets.UTF_8);

        assertThat(errors).isEqualTo("id_003-check,name,surname,2000-1-29,ERROR,DUPLICATE\n");
    }

    @Test
    public void migrate___ShouldCheckForNewClients() throws SQLException {
        String dataToInsert = "<cia>\n" +
                "  <client id=\"id_003-check\">\n" +
                "    <workPhone>+7-201-918-64-13 вн. afm3</workPhone>\n" +
                "    <birth value=\"2000-08-06\"/>\n" +
                "    <mobilePhone>+7-867-609-72-85</mobilePhone>\n" +
                "    <charm value=\"unique_charm\"/>\n" +
                "    <homePhone value=\"+7-194-353-47-03\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"jGq1cYN750zzqQwISySQ\" house=\"7v\" flat=\"zL\"/>\n" +
                "      <register street=\"xMrGcYpmI9rHH32XGBxz83\" house=\"cz\" flat=\"GX\"/>\n" +
                "    </address>\n" +
                "    <name value=\"E8WM1BXEjq\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <surname value=\"18JHY1qHbr\"/>\n" +
                "    <mobilePhone>+7-890-371-42-27</mobilePhone>\n" +
                "    <patronymic value=\"vX3qMhR99VCjN\"/>\n" +
                "    <workPhone>+7-877-942-69-25</workPhone>\n" +
                "  </client>\n" +
                "</cia>";

        migration.inputData = new ByteArrayInputStream(dataToInsert.getBytes(StandardCharsets.UTF_8));

        //
        //
        migration.migrate();
        //
        //

        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT id FROM charm WHERE name = 'unique_charm'");

            int id;

            if (rs.next()) {
                id = rs.getInt("id");
                assertThat(id).isNotEqualTo(0);
            } else throw new AssertionError("unique_charm name not found in charm table");

            ResultSet rs1 = statement.executeQuery("SELECT * FROM client WHERE id = 'id_003-check'");

            if (rs1.next()) {
                String name = rs1.getString("name");
                String surname = rs1.getString("surname");
                String patronymic = rs1.getString("patronymic");
                Date birth_date = rs1.getDate("birth_date");
                String gender = rs1.getString("gender");
                int charm_id = rs1.getInt("charm_id");

                assertThat(name).isEqualTo("E8WM1BXEjq");
                assertThat(surname).isEqualTo("18JHY1qHbr");
                assertThat(patronymic).isEqualTo("vX3qMhR99VCjN");
                assertThat(birth_date).isEqualTo(Date.valueOf("2000-08-06"));
                assertThat(gender).isEqualTo("FEMALE");
                assertThat(charm_id).isEqualTo(id);
            } else throw new AssertionError("client with id id_003-check not found in client table");

            ResultSet rs2 = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check' AND type='FACT'");

            if (rs2.next()) {
                String factStreet = rs2.getString("street");
                String factHouse = rs2.getString("house");
                String factFlat = rs2.getString("flat");

                assertThat(factStreet).isEqualTo("jGq1cYN750zzqQwISySQ");
                assertThat(factHouse).isEqualTo("7v");
                assertThat(factFlat).isEqualTo("zL");
            } else
                throw new AssertionError("address with id id_003-check and type FACT not found in client_addr table");

            ResultSet rs3 = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check' AND type='REG'");

            if (rs3.next()) {
                String regStreet = rs3.getString("street");
                String regHouse = rs3.getString("house");
                String regFlat = rs3.getString("flat");

                assertThat(regStreet).isEqualTo("xMrGcYpmI9rHH32XGBxz83");
                assertThat(regHouse).isEqualTo("cz");
                assertThat(regFlat).isEqualTo("GX");
            } else throw new AssertionError("address with id id_003-check and type REG not found in client_addr table");

            assertPhoneExists("+7-201-918-64-13 вн. afm3", "WORK", "id_003-check");
            assertPhoneExists("+7-867-609-72-85", "MOBILE", "id_003-check");
            assertPhoneExists("+7-194-353-47-03", "HOME", "id_003-check");
            assertPhoneExists("+7-890-371-42-27", "MOBILE", "id_003-check");
            assertPhoneExists("+7-877-942-69-25", "WORK", "id_003-check");
        }
    }

    @Test
    public void migrate___ShouldCheckForUpdatesToExistingClients() throws SQLException {
        String dataToInsert = "<cia>\n" +
                "  <client id=\"id_003-check\">\n" +
                "    <workPhone>+7-201-918-64-13 вн. afm3</workPhone>\n" +
                "    <birth value=\"2000-08-06\"/>\n" +
                "    <mobilePhone>+7-867-609-72-85</mobilePhone>\n" +
                "    <charm value=\"unique_charm\"/>\n" +
                "    <homePhone value=\"+7-194-353-47-03\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"jGq1cYN750zzqQwISySQ\" house=\"7v\" flat=\"zL\"/>\n" +
                "      <register street=\"xMrGcYpmI9rH23HXGBxz83\" house=\"cz\" flat=\"GX\"/>\n" +
                "    </address>\n" +
                "    <name value=\"E8WM1BXEjq\"/>\n" +
                "    <gender value=\"FEMALE\"/>\n" +
                "    <surname value=\"18JHY1qHbr\"/>\n" +
                "    <mobilePhone>+7-890-371-42-27</mobilePhone>\n" +
                "    <patronymic value=\"vX3qMhR99VCjN\"/>\n" +
                "    <workPhone>+7-877-942-69-25</workPhone>\n" +
                "  </client>\n" +
                "</cia>";
        String dataToUpsert = "<cia>\n" +
                "  <client id=\"id_003-check\">\n" +
                "    <birth value=\"2002-12-03\"/>\n" +
                "    <charm value=\"unique_charm\"/>\n" +
                "    <homePhone value=\"+7-708-370-50-95\"/>\n" +
                "    <address>\n" +
                "      <fact street=\"U23zsa2dsa123\" house=\"43\" flat=\"B\"/>\n" +
                "      <register street=\"Alma-Ata\" house=\"8\" flat=\"A\"/>\n" +
                "    </address>\n" +
                "    <name value=\"Andrew\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <surname value=\"Smith\"/>\n" +
                "    <mobilePhone>+7-708-370-50-95</mobilePhone>\n" +
                "    <patronymic value=\"Will\"/>\n" +
                "    <workPhone>+7-708-370-50-95</workPhone>\n" +
                "  </client>\n" +
                "</cia>";

        migration.inputData = new ByteArrayInputStream(dataToInsert.getBytes(StandardCharsets.UTF_8));
        migration.migrate();

        migration.inputData = new ByteArrayInputStream(dataToUpsert.getBytes(StandardCharsets.UTF_8));
        migration.outputStream = new ByteArrayOutputStream();

        //
        //
        migration.migrate();
        //
        //

        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT id FROM charm WHERE name = 'unique_charm'");

            int id;

            if (rs.next()) {
                id = rs.getInt("id");
                assertThat(id).isNotEqualTo(0);
            } else throw new AssertionError("unique_charm name not found in charm table");

            ResultSet rs1 = statement.executeQuery("SELECT * FROM client WHERE id = 'id_003-check'");

            if (rs1.next()) {
                String name = rs1.getString("name");
                String surname = rs1.getString("surname");
                String patronymic = rs1.getString("patronymic");
                Date birth_date = rs1.getDate("birth_date");
                String gender = rs1.getString("gender");
                int charm_id = rs1.getInt("charm_id");

                assertThat(name).isEqualTo("Andrew");
                assertThat(surname).isEqualTo("Smith");
                assertThat(patronymic).isEqualTo("Will");
                assertThat(birth_date).isEqualTo(Date.valueOf("2002-12-03"));
                assertThat(gender).isEqualTo("MALE");
                assertThat(charm_id).isEqualTo(id);
            } else throw new AssertionError("client with id id_003-check not found in client table");

            ResultSet rs2 = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check' AND type='FACT'");

            if (rs2.next()) {
                String factStreet = rs2.getString("street");
                String factHouse = rs2.getString("house");
                String factFlat = rs2.getString("flat");

                assertThat(factStreet).isEqualTo("U23zsa2dsa123");
                assertThat(factHouse).isEqualTo("43");
                assertThat(factFlat).isEqualTo("B");
            } else
                throw new AssertionError("address with id id_003-check and type FACT not found in client_addr table");

            ResultSet rs3 = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check' AND type='REG'");

            if (rs3.next()) {
                String regStreet = rs3.getString("street");
                String regHouse = rs3.getString("house");
                String regFlat = rs3.getString("flat");

                assertThat(regStreet).isEqualTo("Alma-Ata");
                assertThat(regHouse).isEqualTo("8");
                assertThat(regFlat).isEqualTo("A");
            } else throw new AssertionError("address with id id_003-check and type REG not found in client_addr table");

            assertPhoneExists("+7-708-370-50-95", "MOBILE", "id_003-check");
            assertPhoneExists("+7-708-370-50-95", "HOME", "id_003-check");
            assertPhoneExists("+7-708-370-50-95", "WORK", "id_003-check");
        }

    }

}
