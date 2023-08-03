package kz.greetgo.sandboxserver.migration;

import kz.greetgo.sandboxserver.migration.xml_parser.ClientTmp;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CiaMigrationTest {
    private Connection connection;
    private CiaMigration migration;
    private DatabaseAccess dbAccess;

    @BeforeTest
    public void createTables() {
        DatabaseSetup.createSourceTables();
    }

    @BeforeMethod
    public void prepareCiaMigrationAndDbAccess() throws SQLException {
        connection = DatabaseSetup.getConnection();
        dbAccess = new DatabaseAccess(connection);

        createCiaMigration();
        removePreviousTestData();
    }

    private void createCiaMigration() {
        migration = new CiaMigration(connection);
        migration.prepareTmpTables();
        dbAccess.tmpClientTable = migration.tmpClientTable;
        dbAccess.tmpPhoneTable = migration.tmpPhoneTable;
    }

    private void removePreviousTestData() throws SQLException {
        migration.exec("DELETE FROM client_addr WHERE client_cia_id = 'id_003-check'");
        migration.exec("DELETE FROM client_phone WHERE client_cia_id = 'id_003-check'");
        migration.exec("DELETE FROM client WHERE cia_id = 'id_003-check'");
        migration.exec("DELETE FROM charm WHERE name = 'unique_charm'");
    }

    @AfterMethod
    public void closeConnections() {
        try {
            if (migration != null) {
                migration.close();
                migration = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
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
                assertThat(errorMsg).contains(error);
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
             ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM client_phone WHERE client_cia_id = '" + client_id + "' AND type = '" + type + "' AND number = '" + phoneNumber + "'")) {
            if (resultSet.next()) {
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            } else {
                throw new AssertionError("Phone with client_id: " + client_id + ", type: + " + type + ", number: " + phoneNumber + " not found in client_phone table");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void insertClientToTmpTable(ClientTmp client) throws SQLException {
        migration.exec("INSERT INTO TMP_CLIENT (id, client_id, surname, name, patronymic, gender, charm, birth, " +
                        "fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                List.of(
                        client.id, client.client_id, client.surname, client.name, client.patronymic, client.gender, client.charm,
                        client.birth, client.factStreet, client.factHouse, client.factFlat, client.registerStreet,
                        client.registerHouse, client.registerFlat, client.error, client.status)
        );
    }

    private void insertPhoneToTmpTable(ClientTmpPhone clientPhone) throws SQLException {
        migration.exec("INSERT INTO TMP_PHONE (id, client_id, type, number, status) VALUES (?, ?, ?, ?, ?)",
                List.of(clientPhone.id, clientPhone.client_id, clientPhone.type, clientPhone.number, clientPhone.status)
        );
    }

    @Test
    public void validateSurnameAbsence___ShouldCheckThatClientHasSurnameError() throws Exception {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "", // No Surname
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateSurnameAbsence();
        //
        //

        assertClientsHaveError("surname is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateSurnameAbsence___ShouldCheckThatClientHasNoSurnameError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4", // There is Surname
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateSurnameAbsence();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateNameAbsence___ShouldCheckThatClientHasNameError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "", // No Name
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateNameAbsence();
        //
        //

        assertClientsHaveError("name is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateNameAbsence___ShouldCheckThatClientHasNoNameError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9", // There is Name
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateNameAbsence();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateBirthDateAbsence___ShouldCheckThatClientHasBirthDateError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "", // No Birth
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateBirthDateAbsence();
        //
        //

        assertClientsHaveError("birth_date is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateBirthDateAbsence___ShouldCheckThatClientHasNoBirthDateError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28", // There is Birth
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateBirthDateAbsence();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateBirthDate___ShouldCheckThatClientsHaveNotCorrectDateError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "12-12-2002", // Not correct birth, should be (2002-12-12)
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        insertClientToTmpTable(new ClientTmp(
                2,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2002-20-12", // Only 12 months
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateBirthDate();
        //
        //

        assertClientsHaveError("birth_date is not correct", migration.tmpClientTable);
    }

    @Test
    public void validateBirthDate___ClientsShouldHaveIncorrectDateError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2002-02-30", // February only 28 days
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"

        ));
        insertClientToTmpTable(new ClientTmp(
                2,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2002-06-31", // June only 30 days
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateBirthDate();
        //
        //

        assertClientsHaveError("birth_date is not correct", migration.tmpClientTable);
    }

    @Test
    public void validateBirthDate___ShouldCheckThatClientHasCorrectDate() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2002-12-12", // Correct Pattern (YYYY-MM-DD)
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        insertClientToTmpTable(new ClientTmp(
                2,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28", // Correct Pattern (YYYY-MM-DD)
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateBirthDate();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void validateAgeRange___ShouldCheckThatClientHasAgeOutOfRangeError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2022-12-12", // less than 18 y.o
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        insertClientToTmpTable(new ClientTmp(
                2,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "1901-12-30", // more than 100 y.o
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateAgeRange();
        //
        //

        assertClientsHaveError("AGE_OUT_OF_RANGE", migration.tmpClientTable);
    }

    @Test
    public void validateAgeRange___ShouldCheckThatClientHasNoAgeOutOfRangeError() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2005-01-01", // 18 y.o
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.validateAgeRange();
        //
        //

        assertClientsHaveNoError(migration.tmpClientTable);
    }

    @Test
    public void markDuplicateClients___ShouldMark_FirstClientAsDuplicate() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9_DUPLICATE", // Clients added earlier will be marked as duplicates
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2005-01-01",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        insertClientToTmpTable(new ClientTmp(
                2,
                "id_001",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9_NEW", // Should not be duplicate
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2005-01-01",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        insertClientToTmpTable(new ClientTmp(
                3,
                "id_002",
                "surname_bl3X8Nz3q4",
                "name_Mv8feUoXT9_OTHER", // Should not be duplicate
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2005-01-01",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.markDuplicateClients();
        //
        //

        Map<String, String> duplicateClient = dbAccess.getTmpClientById(1);
        Map<String, String> okClient = dbAccess.getTmpClientById(2);
        Map<String, String> otherClient = dbAccess.getTmpClientById(3);


        assertThat(duplicateClient.get("name")).isEqualTo("name_Mv8feUoXT9_DUPLICATE");
        assertThat(duplicateClient.get("status")).isEqualTo("DUPLICATE");

        assertThat(okClient.get("name")).isEqualTo("name_Mv8feUoXT9_NEW");
        assertThat(okClient.get("status")).isEqualTo("JUST INSERTED");

        assertThat(otherClient.get("name")).isEqualTo("name_Mv8feUoXT9_OTHER");
        assertThat(otherClient.get("status")).isEqualTo("JUST INSERTED");
    }

    @Test
    public void markDuplicatePhones___ShouldMark_FirstSamePhonesAsDuplicate() throws SQLException {
        insertPhoneToTmpTable(new ClientTmpPhone(1, "id_001", "HOME", "+7-718-096-63-80 вн. Y2RH", "JUST INSERTED")); // duplicate
        insertPhoneToTmpTable(new ClientTmpPhone(2, "id_001", "HOME", "+7-718-096-63-80 вн. Y2RH", "JUST INSERTED"));
        insertPhoneToTmpTable(new ClientTmpPhone(3, "id_001", "WORK", "+888080130123", "JUST INSERTED")); // duplicate
        insertPhoneToTmpTable(new ClientTmpPhone(4, "id_001", "WORK", "+888080130123", "JUST INSERTED"));
        insertPhoneToTmpTable(new ClientTmpPhone(5, "id_001", "MOBILE", "+123123123123", "JUST INSERTED")); // duplicate
        insertPhoneToTmpTable(new ClientTmpPhone(6, "id_001", "MOBILE", "+123123123123", "JUST INSERTED"));

        //
        //
        migration.markDuplicatePhones();
        //
        //

        Map<String, String> duplicateHomePhone = dbAccess.getTmpPhoneById(1);
        Map<String, String> okHomePhone = dbAccess.getTmpPhoneById(2);
        Map<String, String> duplicateWorkPhone = dbAccess.getTmpPhoneById(3);
        Map<String, String> okWorkPhone = dbAccess.getTmpPhoneById(4);
        Map<String, String> duplicateMobilePhone = dbAccess.getTmpPhoneById(5);
        Map<String, String> okMobilePhone = dbAccess.getTmpPhoneById(6);


        assertThat(duplicateHomePhone.get("status")).isEqualTo("DUPLICATE");
        assertThat(okHomePhone.get("status")).isEqualTo("JUST INSERTED");

        assertThat(duplicateWorkPhone.get("status")).isEqualTo("DUPLICATE");
        assertThat(okWorkPhone.get("status")).isEqualTo("JUST INSERTED");

        assertThat(duplicateMobilePhone.get("status")).isEqualTo("DUPLICATE");
        assertThat(okMobilePhone.get("status")).isEqualTo("JUST INSERTED");
    }

    @Test
    public void uploadToTmp___ShouldCheckThatClientUploadedCorrectly() throws SQLException {
        String str = "" +
                "<cia>" +
                "  <client id=\"id_003-check\">" +
                "    <surname value=\"snv435hv5\"/>" +
                "    <birth value=\"12-12-2002\"/>" +
                "    <name value=\"asd23ad2asw\"/>" +
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

        //
        //
        migration.uploadToTmp();
        //
        //

        Map<String, String> map = dbAccess.getTmpClientById(1);

        assertThat(map.get("client_id")).isEqualTo("id_003-check");
        assertThat(map.get("surname")).isEqualTo("snv435hv5");
        assertThat(map.get("birth")).isEqualTo("12-12-2002");
        assertThat(map.get("name")).isEqualTo("asd23ad2asw");
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

    @Test
    public void insertDataFromTmpToCharm___ShouldCheckThatCharmInsertedCorrectly() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check",
                "surname",
                "name",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR INSERT"
        ));

        //
        //
        migration.insertDataFromTmpToCharm();
        //
        //

        int charmCount = dbAccess.getSourceCharmCountByName("unique_charm");
        assertThat(charmCount).isEqualTo(1);

        int charmId = dbAccess.getSourceCharmIdByName("unique_charm");
        assertThat(charmId).isNotZero();
    }

    @Test
    public void upsertDataToCharm___ShouldCheckForCharmNameUniqueness() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check",
                "surname",
                "name",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR INSERT"
        ));

        migration.insertDataFromTmpToCharm();

        insertClientToTmpTable(new ClientTmp(
                2,
                "id_003-check",
                "surname",
                "name",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR INSERT"
        ));

        //
        //
        migration.insertDataFromTmpToCharm();
        //
        //

        int charmCount = dbAccess.getSourceCharmCountByName("unique_charm");
        assertThat(charmCount).isEqualTo(1);
    }

    @Test
    public void insertDataToClient___ShouldCheckThatClientInsertedCorrectly() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check",
                "surname",
                "name",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR INSERT"
        ));
        migration.insertDataFromTmpToCharm(); // client table has FK charm_id

        //
        //
        migration.insertDataFromTmpToClient();
        //
        //

        Map<String, String> map = dbAccess.getSourceClientByCiaId("id_003-check");

        assertThat(map.get("cia_id")).isEqualTo("id_003-check");
        assertThat(map.get("surname")).isEqualTo("surname");
        assertThat(map.get("name")).isEqualTo("name");
        assertThat(map.get("patronymic")).isEqualTo("patronymic");
        assertThat(map.get("gender")).isEqualTo("MALE");
        assertThat(map.get("birth_date")).isEqualTo("2000-01-29");
        assertThat(map.get("charm")).isEqualTo("unique_charm");
    }

    @Test
    public void markUpdateAndInsertClients___ShouldMarkClientSecond_AsForUpdate() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check", // Will be existing Client
                "surname",
                "Hj4YCQqX4u__OLD_NAME ",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR INSERT"
        ));
        migration.insertDataFromTmpToClient();

        insertClientToTmpTable(new ClientTmp(
                2,
                "id_003-check",
                "surname",
                "Hj4YCQqX4u__OTHER_NAME",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED" // Should be Marked As For Update
        ));

        //
        //
        migration.markUpdateAndInsertClients();
        //
        //


        Map<String, String> map = dbAccess.getTmpClientById(2);

        assertThat(map.get("name")).isEqualTo("Hj4YCQqX4u__OTHER_NAME");
        assertThat(map.get("status")).isEqualTo("FOR UPDATE");
    }

    @Test
    public void markUpdateAndInsertClients___ShouldMarkSecondClient_AsForInsert() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check", // Will be existing Client
                "surname",
                "Hj4YCQqX4u__OLD_NAME ",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR INSERT"
        ));
        migration.insertDataFromTmpToClient();

        insertClientToTmpTable(new ClientTmp(
                2,
                "id_004-check", // Another Client
                "surname",
                "Hj4YCQqX4u__OTHER_NAME",
                "patronymic",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED" // This Should be Marked As For Insert
        ));

        //
        //
        migration.markUpdateAndInsertClients();
        //
        //

        Map<String, String> map = dbAccess.getTmpClientById(2);

        assertThat(map.get("status")).isEqualTo("FOR INSERT");
        assertThat(map.get("name")).isEqualTo("Hj4YCQqX4u__OTHER_NAME");
    }

    @Test
    public void updateDataInClient___ShouldCheckForUpdatesOf_ClientFields() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check", // Will be existing Client
                "OLD_SURNAME",
                "OLD_NAME",
                "OLD_PATRONYMIC",
                "FEMALE",
                "unique_charm",
                "2002-01-30",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR INSERT"
        ));
        migration.insertDataFromTmpToCharm();
        migration.insertDataFromTmpToClient();

        {
            Map<String, String> map = dbAccess.getSourceClientByCiaId("id_003-check");

            assertThat(map.get("cia_id")).isEqualTo("id_003-check");
            assertThat(map.get("surname")).isEqualTo("OLD_SURNAME");
            assertThat(map.get("name")).isEqualTo("OLD_NAME");
            assertThat(map.get("patronymic")).isEqualTo("OLD_PATRONYMIC");
            assertThat(map.get("birth_date")).isEqualTo("2002-01-30");
            assertThat(map.get("charm")).isEqualTo("unique_charm");
            assertThat(map.get("gender")).isEqualTo("FEMALE");
        }

        insertClientToTmpTable(new ClientTmp(
                2,
                "id_003-check", // Will be updating Client
                "NEW_SURNAME",
                "NEW_NAME",
                "NEW_PATRONYMIC",
                "MALE",
                "choleric",
                "1999-09-19",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "FOR UPDATE"
        ));
        migration.insertDataFromTmpToCharm();

        //
        //
        migration.updateDataInClientFromTmp();
        //
        //

        Map<String, String> map = dbAccess.getSourceClientByCiaId("id_003-check");

        assertThat(map.get("cia_id")).isEqualTo("id_003-check");
        assertThat(map.get("surname")).isEqualTo("NEW_SURNAME");
        assertThat(map.get("name")).isEqualTo("NEW_NAME");
        assertThat(map.get("patronymic")).isEqualTo("NEW_PATRONYMIC");
        assertThat(map.get("birth_date")).isEqualTo("1999-09-19");
        assertThat(map.get("charm")).isEqualTo("choleric");
        assertThat(map.get("gender")).isEqualTo("MALE");
    }

    @Test
    public void upsertDataToClientAddr___ShouldCheckForUpdatesOf_ClientAddrFields() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check",
                "surname_iBmEgMEL39",
                "name_pp30c98QU6",
                "patronymic_69pfIfc7Jn",
                "MALE",
                "unique_charm",
                "2000-01-29",
                "", // Addresses are empty
                "",
                "",
                "",
                "",
                "",
                "",
                "FOR INSERT"
        ));

        migration.insertDataFromTmpToClient();
        migration.upsertDataFromTmpToClientAddr();

        {
            Map<String, String> oldReg = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "REG");
            Map<String, String> oldFact = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "FACT");

            assertThat(oldFact.get("street")).isEqualTo("");
            assertThat(oldFact.get("house")).isEqualTo("");
            assertThat(oldFact.get("flat")).isEqualTo("");
            assertThat(oldReg.get("street")).isEqualTo("");
            assertThat(oldReg.get("house")).isEqualTo("");
            assertThat(oldReg.get("flat")).isEqualTo("");
        }

        createCiaMigration(); // 2nd Migration Started

        insertClientToTmpTable(new ClientTmp(
                2,
                "id_003-check",
                "surname_iBmEgMEL39",
                "name_pp30c98QU6",
                "patronymic_69pfIfc7Jn",
                "MALE",
                "choleric",
                "2022-12-30",
                "asd st.",  // Addresses are not empty
                "145B",
                "7th floor",
                "qwe st.",
                "43B",
                "3th floor",
                "",
                "JUST INSERTED"
        ));

        //
        //
        migration.upsertDataFromTmpToClientAddr();
        //
        //

        {
            Map<String, String> newReg = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "REG");
            Map<String, String> newFact = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "FACT");

            assertThat(newFact.get("street")).isEqualTo("asd st.");
            assertThat(newFact.get("house")).isEqualTo("145B");
            assertThat(newFact.get("flat")).isEqualTo("7th floor");
            assertThat(newReg.get("street")).isEqualTo("qwe st.");
            assertThat(newReg.get("house")).isEqualTo("43B");
            assertThat(newReg.get("flat")).isEqualTo("3th floor");

        }
    }

    @Test
    public void upsertDataToClientPhone___ShouldCheckIfClientPhonesExist() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_003-check",
                "surname_iBmEgMEL39",
                "name_pp30c98QU6",
                "patronymic_69pfIfc7Jn",
                "MALE",
                "choleric",
                "2022-12-30",
                "asd st.",
                "145B",
                "7th floor",
                "qwe st.",
                "43B",
                "3th floor",
                "",
                "FOR INSERT"
        ));
        insertPhoneToTmpTable(new ClientTmpPhone(1, "id_003-check", "HOME", "+7-718-096-63-80 вн. Y2RH", "JUST INSERTED"));
        insertPhoneToTmpTable(new ClientTmpPhone(2, "id_003-check", "WORK", "+7-708-304-22-23", "JUST INSERTED"));
        insertPhoneToTmpTable(new ClientTmpPhone(3, "id_003-check", "MOBILE", "+888080130123", "JUST INSERTED"));
        insertPhoneToTmpTable(new ClientTmpPhone(4, "id_003-check", "WORK", "+123123123123", "JUST INSERTED"));
        insertPhoneToTmpTable(new ClientTmpPhone(5, "id_003-check", "MOBILE", "+321321321", "JUST INSERTED"));

        migration.insertDataFromTmpToClient(); // ClientPhone table has client_id FK

        //
        //
        migration.insertDataFromTmpToClientPhone();
        //
        //

        assertPhoneExists("+7-718-096-63-80 вн. Y2RH", "HOME", "id_003-check");
        assertPhoneExists("+321321321", "MOBILE", "id_003-check");
        assertPhoneExists("+888080130123", "MOBILE", "id_003-check");
        assertPhoneExists("+7-708-304-22-23", "WORK", "id_003-check");
        assertPhoneExists("+123123123123", "WORK", "id_003-check");
    }

    @Test
    public void uploadErrors___ShouldCheckErrorUploadAfterWithInvalidSurname() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "", // No Surname
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        migration.outputErrors = outputErrors;
        migration.validateSurnameAbsence();

        //
        //
        migration.uploadErrors();
        //
        //

        String errors = outputErrors.toString(StandardCharsets.UTF_8);

        assertThat(errors).contains("id_001,name_Mv8feUoXT9,,2000-02-28,ERROR,");
        assertThat(errors).contains("surname is not defined\n");
    }

    @Test
    public void uploadErrors___ShouldCheckErrorUploadWithInvalidName() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_gO01nSVG1w",
                "", // No Name
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2000-02-28",
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        migration.outputErrors = outputErrors;
        migration.validateNameAbsence();

        //
        //
        migration.uploadErrors();
        //
        //

        String errors = outputErrors.toString(StandardCharsets.UTF_8);

        assertThat(errors).contains("id_001,,surname_gO01nSVG1w,2000-02-28,ERROR,");
        assertThat(errors).contains("name is not defined\n");
    }

    @Test
    public void uploadErrors___ShouldCheckErrorUploadWithEmptyBirthDate() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_gO01nSVG1w",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "", // No birth
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        migration.outputErrors = outputErrors;
        migration.validateBirthDateAbsence();

        //
        //
        migration.uploadErrors();
        //
        //

        String errors = outputErrors.toString(StandardCharsets.UTF_8);

        assertThat(errors).contains("id_001,name_Mv8feUoXT9,surname_gO01nSVG1w,,ERROR,");
        assertThat(errors).contains("birth_date is not defined\n");
    }

    @Test
    public void uploadErrors___ShouldCheckErrorUploadWithInvalidBirthDate() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_gO01nSVG1w",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "21-12-2002", // Should be yyyy-MM-dd
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        migration.outputErrors = outputErrors;
        migration.validateBirthDate();

        //
        //
        migration.uploadErrors();
        //
        //

        String errors = outputErrors.toString(StandardCharsets.UTF_8);

        assertThat(errors).contains("id_001,name_Mv8feUoXT9,surname_gO01nSVG1w,21-12-2002,ERROR,");
        assertThat(errors).contains("birth_date is not correct\n");
    }

    @Test
    public void uploadErrors___ShouldCheckErrorUploadWithInvalidAgeRange() throws SQLException {
        insertClientToTmpTable(new ClientTmp(
                1,
                "id_001",
                "surname_gO01nSVG1w",
                "name_Mv8feUoXT9",
                "patronymic_1pHbfGhcRd",
                "MALE",
                "choleric",
                "2022-12-12", // Too young
                "asd st.",
                "145B",
                "7th floor",
                "Tole bi st.",
                "122A",
                "1st floor",
                "",
                "JUST INSERTED"
        ));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        migration.outputErrors = outputErrors;
        migration.validateAgeRange();

        //
        //
        migration.uploadErrors();
        //
        //

        String errors = outputErrors.toString(StandardCharsets.UTF_8);

        assertThat(errors).contains("id_001,name_Mv8feUoXT9,surname_gO01nSVG1w,2022-12-12,ERROR,");
        assertThat(errors).contains("AGE_OUT_OF_RANGE\n");
    }

    @Test
    public void migrate___ShouldCheckForNewClients() throws SQLException {
        String dataToInsert = "" +
                "<cia>\n" +
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

        int id = dbAccess.getSourceCharmIdByName("unique_charm");
        assertThat(id).isNotEqualTo(0);

        Map<String, String> client = dbAccess.getSourceClientByCiaId("id_003-check");
        assertThat(client.get("name")).isEqualTo("E8WM1BXEjq");
        assertThat(client.get("surname")).isEqualTo("18JHY1qHbr");
        assertThat(client.get("patronymic")).isEqualTo("vX3qMhR99VCjN");
        assertThat(client.get("birth_date")).isEqualTo(Date.valueOf("2000-08-06").toString());
        assertThat(client.get("gender")).isEqualTo("FEMALE");
        assertThat(client.get("charm_id")).isEqualTo("" + id);

        Map<String, String> fact = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "FACT");
        assertThat(fact.get("street")).isEqualTo("jGq1cYN750zzqQwISySQ");
        assertThat(fact.get("house")).isEqualTo("7v");
        assertThat(fact.get("flat")).isEqualTo("zL");


        Map<String, String> reg = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "REG");
        assertThat(reg.get("street")).isEqualTo("xMrGcYpmI9rHH32XGBxz83");
        assertThat(reg.get("house")).isEqualTo("cz");
        assertThat(reg.get("flat")).isEqualTo("GX");

        assertPhoneExists("+7-201-918-64-13 вн. afm3", "WORK", "id_003-check");
        assertPhoneExists("+7-867-609-72-85", "MOBILE", "id_003-check");
        assertPhoneExists("+7-194-353-47-03", "HOME", "id_003-check");
        assertPhoneExists("+7-890-371-42-27", "MOBILE", "id_003-check");
        assertPhoneExists("+7-877-942-69-25", "WORK", "id_003-check");
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
        migration.outputErrors = new ByteArrayOutputStream();

        //
        //
        migration.migrate();
        //
        //

        int id = dbAccess.getSourceCharmIdByName("unique_charm");
        assertThat(id).isNotEqualTo(0);

        Map<String, String> client = dbAccess.getSourceClientByCiaId("id_003-check");
        assertThat(client.get("name")).isEqualTo("Andrew");
        assertThat(client.get("surname")).isEqualTo("Smith");
        assertThat(client.get("patronymic")).isEqualTo("Will");
        assertThat(client.get("birth_date")).isEqualTo(Date.valueOf("2002-12-03").toString());
        assertThat(client.get("gender")).isEqualTo("MALE");
        assertThat(client.get("charm_id")).isEqualTo("" + id);

        Map<String, String> fact = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "FACT");
        assertThat(fact.get("street")).isEqualTo("U23zsa2dsa123");
        assertThat(fact.get("house")).isEqualTo("43");
        assertThat(fact.get("flat")).isEqualTo("B");
        
        Map<String, String> reg = dbAccess.getSourceClientAddrByIdAndType("id_003-check", "REG");
        assertThat(reg.get("street")).isEqualTo("Alma-Ata");
        assertThat(reg.get("house")).isEqualTo("8");
        assertThat(reg.get("flat")).isEqualTo("A");

        assertPhoneExists("+7-708-370-50-95", "MOBILE", "id_003-check");
        assertPhoneExists("+7-708-370-50-95", "HOME", "id_003-check");
        assertPhoneExists("+7-708-370-50-95", "WORK", "id_003-check");
    }

}
