package kz.greetgo.sandboxserver.migration;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CiaMigrationTest {
    private Connection connection;
    private CiaMigration migration;

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

    private void assertClientHasError(String error, String tmpClientTable) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM " + tmpClientTable);
            if (resultSetC.next()) {
                String status = resultSetC.getString("status");
                String errorMsg = resultSetC.getString("error");

                assertThat(status).isEqualTo("ERROR");
                assertThat(errorMsg).isEqualTo(error);
            } else {
                throw new AssertionError("No client found in " + tmpClientTable);
            }
        }
    }

    private void assertClientsHasError(String error, String tmpClientTable) throws SQLException {
        boolean foundClient = false;

        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
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


    @Test
    public void validateSurnameAbsence___ShouldFlagClientAsError() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', '', " + // HERE
                "'name', 'patronymic', 'MALE', 'holeric', '2000-2-31', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);

        //
        //
        migration.validateSurnameAbsence();
        //
        //

        assertClientHasError("surname is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateNameAbsence___ShouldFlagClientAsError() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', 'surname', '', " + // HERE
                "'patronymic', 'MALE', 'holeric', '2000-2-31', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);

        //
        //
        migration.validateNameAbsence();
        //
        //

        assertClientHasError("name is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateBirthDateAbsence___ShouldFlagClientAsError() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', 'surname', 'name', 'patronymic', 'MALE', 'holeric', ''," + // HERE
                " 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);

        //
        //
        migration.validateBirthDateAbsence();
        //
        //

        assertClientHasError("birth_date is not defined", migration.tmpClientTable);
    }

    @Test
    public void validateBirthDatePattern___ShouldFlagClientsAsError() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', '', 'name', 'patronymic', 'MALE', 'holeric', '12-12-2002', " + // HERE (YYYY-MM-DD)
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String dataToInsert2 = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', '', 'name', 'patronymic', 'MALE', 'holeric', '2002-1-1', " + // HERE (YYYY-MM-DD)
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String dataToInsert3 = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', '', 'name', 'patronymic', 'MALE', 'holeric', '2002-2-30', " + // HERE (February only 28 days)
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);
        migration.exec(dataToInsert2);
        migration.exec(dataToInsert3);

        //
        //
        migration.validateBirthDatePattern();
        //
        //

        assertClientsHasError("birth_date is not correct", migration.tmpClientTable);
    }

    @Test
    public void validateAgeRange___ShouldFlagClientsAsError() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', '', 'name', 'patronymic', 'MALE', 'holeric', '2022-12-30', " + // HERE (Too young)
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String dataToInsert2 = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_002', '', 'name', 'patronymic', 'MALE', 'holeric', '1901-12-30', " + // HERE (Too old)
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);
        migration.exec(dataToInsert2);

        //
        //
        migration.validateAgeRange();
        //
        //

        assertClientsHasError("AGE_OUT_OF_RANGE", migration.tmpClientTable);
    }

    @Test
    public void validateDuplicateClients___ShouldFlagClientAsError() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', 'surname', 'name_duplicate', " + // Clients added earlier will be marked as duplicates
                "'patronymic', 'MALE', 'holeric', '2000-2-31', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String dataToInsert2 = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', 'surname', 'name_not_duplicate', 'patronymic', 'MALE', 'holeric', '2000-2-31', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);
        migration.exec(dataToInsert2);

        //
        //
        migration.validateDuplicateClients();
        //
        //

        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM " + migration.tmpClientTable + " WHERE status = 'ERROR'");
            if (resultSetC.next()) {
                String errorMsg = resultSetC.getString("error");
                String name = resultSetC.getString("name");

                assertThat(errorMsg).isEqualTo("DUPLICATE");
                assertThat(name).isEqualTo("name_duplicate");
            } else {
                throw new AssertionError("No client found in " + migration.tmpClientTable);
            }
        }

    }

    @Test
    public void validateDuplicatePhones___ShouldFlagClientAsError() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_001', 'surname', 'name_duplicate', " + // Clients added earlier will be marked as duplicates
                "'patronymic', 'MALE', 'holeric', '2000-2-31', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String phonesToInsert = "INSERT INTO TMP_PHONE (client_id, type, number) VALUES " +
                "('id_001', 'HOME', '+7-718-096-63-80 вн. Y2RH'), " +
                "('id_001', 'HOME', '+7-718-096-63-80 вн. Y2RH'), " +
                "('id_001', 'WORK', '+888080130123'), " +
                "('id_001', 'WORK', '+888080130123'), " +
                "('id_001', 'MOBILE', '+123123123123'), " +
                "('id_001', 'MOBILE', '+123123123123')";

        migration.prepareStorages();
        migration.exec(dataToInsert);
        migration.exec(phonesToInsert);

        //
        //
        migration.validateDuplicatePhones();
        //
        //

        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSetC = statement.executeQuery("SELECT count(*) FROM " + migration.tmpPhoneTable);
            if (resultSetC.next()) {
                assertThat(resultSetC.getInt(1)).isEqualTo(3);
            } else {
                throw new AssertionError("No client found in " + migration.tmpClientTable);
            }
        }

    }

    @Test
    public void download___shouldDownloadClientToTmp() throws SQLException {
        String str = "<cia><client id=\"id_003-check\">" +
                "<surname value=\"s\"/>" +
                "<birth value=\"12-12-2002\"/>" +
                "<name value=\"n\"/>" +
                "<address>" +
                "<fact street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                "<register street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                "</address>" +
                "<gender value=\"FEMALE\"/>" +
                "<workPhone>+7-165-867-45-80</workPhone>" +
                "<mobilePhone>+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "<homePhone value=\"+7-718-096-63-20\"/>" +
                "<charm value=\"4S7UG5gvok\"/>" +
                "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "</client></cia>";
        migration.inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        migration.prepareStorages();

        //
        //
        migration.download();
        //
        //

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + migration.tmpClientTable)) {
            if (resultSet.next()) {
                String clientId = resultSet.getString("client_id");
                String surname = resultSet.getString("surname");
                String birth = resultSet.getString("birth");
                String name = resultSet.getString("name");
                String factStreet = resultSet.getString("fact_street");
                String regStreet = resultSet.getString("register_street");
                String facHouse = resultSet.getString("fact_house");
                String regHouse = resultSet.getString("register_house");
                String facFlat = resultSet.getString("fact_flat");
                String regFlat = resultSet.getString("register_flat");
                String charm = resultSet.getString("charm");
                String patronymic = resultSet.getString("patronymic");
                String gender = resultSet.getString("gender");

                assertThat(clientId).isEqualTo("id_003-check");
                assertThat(surname).isEqualTo("s");
                assertThat(birth).isEqualTo("12-12-2002");
                assertThat(name).isEqualTo("n");
                assertThat(factStreet).isEqualTo("1CbPis8PMcGfcBSTQzap");
                assertThat(regStreet).isEqualTo("Fh5PGzqIoxXK6r8JgzqH");
                assertThat(facHouse).isEqualTo("s5");
                assertThat(regHouse).isEqualTo("Jq");
                assertThat(facFlat).isEqualTo("Di");
                assertThat(regFlat).isEqualTo("ta");
                assertThat(charm).isEqualTo("4S7UG5gvok");
                assertThat(patronymic).isEqualTo("sn7FcW6YHyhRo");
                assertThat(gender).isEqualTo("FEMALE");
            } else {
                throw new AssertionError("No client found in TMP_CLIENT");
            }
        }
        assertPhoneExists("+7-165-867-45-80", "WORK");
        assertPhoneExists("+7-718-096-63-80 вн. Y2RH", "MOBILE");
        assertPhoneExists("+7-718-096-63-20", "HOME");
    }

    @Test(invocationCount = 2)
    public void insertDataToClient___ShouldInsertClientFromTmpClient() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'holeric', '2000-1-29', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        CiaMigration migration = new CiaMigration(connection);
        migration.prepareStorages();
        migration.exec(dataToInsert);

        //
        //
        migration.insertDataToClient();
        //
        //

        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSetC = statement.executeQuery("SELECT count(*) FROM client WHERE id = 'id_003-check'");
            if (resultSetC.next()) {
                assertThat(resultSetC.getInt(1)).isGreaterThan(0);
            } else {
                throw new AssertionError("No client found in client table");
            }
        }
    }

    @Test()
    public void upsertDataToCharm___ShouldIgnoreDuplicateCharmName() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'unique_charm', " + // HERE
                "'2000-1-29', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String dataToUpsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name_not_duplicate', 'patronymic', 'MALE', 'unique_charm', " + // HERE
                "'2000-1-29', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);
        migration.upsertDataToCharm();

        CiaMigration migration2 = new CiaMigration(connection);
        migration2.prepareStorages();
        migration2.exec(dataToUpsert);

        //
        //
        migration2.upsertDataToCharm();
        //
        //

        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM charm WHERE name = 'unique_charm'");
            if (resultSet.next()) {
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            } else {
                throw new AssertionError("No client found in client table");
            }
        }
    }

    @Test()
    public void updateDuplicatesInClient___ShouldUpdateOldValues_IfIdDuplicated() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', " + // Clients added earlier will be marked as duplicates
                "'patronymic', 'MALE', 'holeric', '2000-1-29', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String dataToUpsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'new_name', 'patronymic', 'MALE', 'holeric', '2000-1-29', 'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        migration.prepareStorages();
        migration.exec(dataToInsert);
        migration.insertDataToClient();

        CiaMigration migration2 = new CiaMigration(connection);
        migration2.outputStream = new ByteArrayOutputStream();
        migration2.prepareStorages();
        migration2.exec(dataToUpsert);

        //
        //
        migration2.updateDuplicatesInClient();
        //
        //

        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client WHERE id = 'id_003-check'");
            if (resultSet.next()) {
                assertThat(resultSet.getString("name")).isEqualTo("new_name");
            } else {
                throw new AssertionError("No client found in client table");
            }
        }
    }

    @Test()
    public void upsertDataToClientAddr___shouldUpsertNewAddressFromTmpClient() throws SQLException {
        String dataToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'unique_charm', '2000-1-29', " +
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String dataToUpsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name_not_duplicate', 'patronymic', 'MALE', 'unique_charm', '2000-1-29', " +
                "'', '', '', '', '', '','','JUST INSERTED')"; // Should be empty

        migration.prepareStorages();
        migration.exec(dataToInsert);
        migration.insertDataToClient();

        CiaMigration migration2 = new CiaMigration(connection);
        migration2.prepareStorages();
        migration2.exec(dataToUpsert);

        //
        //
        migration2.upsertDataToClientAddr();
        //
        //

        try (Statement statement = connection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check'");
            if (resultSet.next()) {
                assertThat(resultSet.getString("street")).isEqualTo("");
                assertThat(resultSet.getString("house")).isEqualTo("");
                assertThat(resultSet.getString("flat")).isEqualTo("");
            } else {
                throw new AssertionError("No client address found");
            }
        }
    }

    @Test
    public void upsertDataToClientPhone___ShouldUpsertPhonesFromTmpPhone() throws SQLException {
        String clientToInsert = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'unique_charm', '2000-1-29', " +
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
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
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";
        String clientToInsertCopy = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) " +
                "VALUES ('id_003-check', 'surname', 'name', 'patronymic', 'MALE', 'unique_charm', '2000-1-29', " +
                "'Manasa st.', '145B', '7th floor', 'Tole bi st.', '122A', '1st floor','','JUST INSERTED')";

        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        migration.prepareStorages();
        migration.outputStream = outputErrors;
        migration.exec(clientToInsert);
        migration.exec(clientToInsertCopy);
        migration.validateDuplicateClients();

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
                "      <register street=\"xMrGcYpmI9rHHXGBxz83\" house=\"cz\" flat=\"GX\"/>\n" +
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

            if(rs.next()){
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
            } else throw new AssertionError("address with id id_003-check and type FACT not found in client_addr table");

            ResultSet rs3 = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check' AND type='REG'");

            if (rs3.next()) {
                String regStreet = rs3.getString("street");
                String regHouse = rs3.getString("house");
                String regFlat = rs3.getString("flat");

                assertThat(regStreet).isEqualTo("xMrGcYpmI9rHHXGBxz83");
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
                "      <register street=\"xMrGcYpmI9rHHXGBxz83\" house=\"cz\" flat=\"GX\"/>\n" +
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
                "      <fact street=\"Ustazdar\" house=\"43\" flat=\"B\"/>\n" +
                "      <register street=\"Alma-Atynskaya\" house=\"8\" flat=\"A\"/>\n" +
                "    </address>\n" +
                "    <name value=\"Temirlan\"/>\n" +
                "    <gender value=\"MALE\"/>\n" +
                "    <surname value=\"Zhumagulov\"/>\n" +
                "    <mobilePhone>+7-708-370-50-95</mobilePhone>\n" +
                "    <patronymic value=\"Aidosovich\"/>\n" +
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

            if(rs.next()){
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

                assertThat(name).isEqualTo("Temirlan");
                assertThat(surname).isEqualTo("Zhumagulov");
                assertThat(patronymic).isEqualTo("Aidosovich");
                assertThat(birth_date).isEqualTo(Date.valueOf("2002-12-03"));
                assertThat(gender).isEqualTo("MALE");
                assertThat(charm_id).isEqualTo(id);
            } else throw new AssertionError("client with id id_003-check not found in client table");

            ResultSet rs2 = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check' AND type='FACT'");

            if (rs2.next()) {
                String factStreet = rs2.getString("street");
                String factHouse = rs2.getString("house");
                String factFlat = rs2.getString("flat");

                assertThat(factStreet).isEqualTo("Ustazdar");
                assertThat(factHouse).isEqualTo("43");
                assertThat(factFlat).isEqualTo("B");
            } else throw new AssertionError("address with id id_003-check and type FACT not found in client_addr table");

            ResultSet rs3 = statement.executeQuery("SELECT * FROM client_addr WHERE client = 'id_003-check' AND type='REG'");

            if (rs3.next()) {
                String regStreet = rs3.getString("street");
                String regHouse = rs3.getString("house");
                String regFlat = rs3.getString("flat");

                assertThat(regStreet).isEqualTo("Alma-Atynskaya");
                assertThat(regHouse).isEqualTo("8");
                assertThat(regFlat).isEqualTo("A");
            } else throw new AssertionError("address with id id_003-check and type REG not found in client_addr table");

            assertPhoneExists("+7-708-370-50-95", "MOBILE", "id_003-check");
            assertPhoneExists("+7-708-370-50-95", "HOME", "id_003-check");
            assertPhoneExists("+7-708-370-50-95", "WORK", "id_003-check");
        }

    }

}
