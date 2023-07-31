package kz.greetgo.sandboxserver.migration;

import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MySAXHandlerTest {
    private Connection connection;
    private PreparedStatement ciaPS;
    private PreparedStatement phonesPS;

    private String tmpClientTable;
    private String tmpPhoneTable;

    @BeforeMethod
    public void setUp() {
        connection = DatabaseSetup.getConnection();

        // Create unique table names for each test
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        Date nowDate = new Date();
        tmpClientTable = "tmp_" + sdf.format(nowDate) + "_client";
        tmpPhoneTable = "tmp_" + sdf.format(nowDate) + "_phone";

        // Create tables
        DatabaseSetup.dropCreateTables(tmpClientTable, tmpPhoneTable);

        // Create prepared statements
        String insertClientSQL = "INSERT INTO " + tmpClientTable + " (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO " + tmpPhoneTable + " (client_id, type, number) VALUES (?,?,?)";

        try {
            ciaPS = connection.prepareStatement(insertClientSQL);
            phonesPS = connection.prepareStatement(insertPhonesPS);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterMethod
    public void tearDown() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
            if(ciaPS != null){
                ciaPS.close();
                ciaPS = null;
            }
            if(phonesPS != null){
                phonesPS.close();
                phonesPS = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void assertPhoneExists(String phoneNumber, String type) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + tmpPhoneTable + " WHERE type = '" + type + "' AND number = '"+phoneNumber+"'")) {
            if (resultSet.next()) {
                Assertions.assertThat(resultSet.getInt(1)).isEqualTo(1);
            } else {
                throw new AssertionError("Phone with type " + type + " and number " + phoneNumber + " not found in " + tmpPhoneTable);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void parse___ShouldLoad_XMLClientData_ToTmpClientTable() throws SQLException {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
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
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);


        //
        //
        mySAXHandler.parse(byteArrayInputStream, null);
        //
        //

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tmpClientTable)) {
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

                assertThat(clientId).isEqualTo("1-IP3-43-PF-GNsIekwnHR");
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
    }
    @Test
    public void parse___ShouldLoad_XMLClientPhones_ToTmpPhoneTable() {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
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
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(byteArrayInputStream, null);
        //
        //

        assertPhoneExists("+7-165-867-45-80", "WORK");
        assertPhoneExists("+7-718-096-63-80 вн. Y2RH", "MOBILE");
        assertPhoneExists("+7-718-096-63-20", "HOME");
    }

    @Test
    public void parse___ShouldLogError_When_MobilePhoneWithoutMatchingEndTag() {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                "<surname value=\"s\"/>" +
                "<birth value=\"12-12-2002\"/>" +
                "<name value=\"n\"/>" +
                "<address>" +
                "<fact street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                "<register street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                "</address>" +
                "<gender value=\"FEMALE\"/>" +
                "<workPhone>+7-165-867-45-80</workPhone>" +
                "<mobilePhone>+7-718-096-63-80 вн. Y2RH" + // Without closing mobile phone tag
                "<homePhone value=\"+7-718-096-63-20\"/>" +
                "<charm value=\"4S7UG5gvok\"/>" +
                "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "</client></cia>";
        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();

        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 447: The element type \"mobilePhone\" must be terminated by the matching end-tag \"</mobilePhone>\".\n");
    }

    @Test
    public void parse___shouldLogError_When_NotWellFormedDocument() {
        String str = "<<client id=\"1-IP3-43-PF-GNsIekwnHR\">>\n" + // HERE
                        "   <birth value=\"15-12-2002\"/>\n" +
                        "   <address mine=\"asd\">\n" +
                        "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>\n" +
                        "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>\n" +
                        "   </address>\n" +
                        "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>\n" +
                        "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH\n</mobilePhone>" +
                        "   <gender value=\"FEMALE\"/>\n" +
                        "   <surname value=\"Gh9g8EvSVo\"/>\n" +
                        "   <name value=\"Azx1qg8EvSVo\"/>\n" +
                        "   <charm value=\"4S7UG5gvok\"/>\n" +
                        "   <workPhone>+7-165-867-45-80</workPhone>\n" +
                        "   <mobilePhone>+7-903-297-09-92</mobilePhone>\n" +
                        "   <patronymic value=\"sn7FcW6YHyhRo\"/>\n" +
                        "</client>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 2: The markup in the document preceding the root element must be well-formed.\n");
    }

    @Test
    public void parse___ShouldLogError_When_ClientIdQuoteIsNotClosed() {
        String str = "<client id=\"1-IP3-43-PF-GNsIekwnHR>" + // HERE
                        "   <birth value=\"15-12-2002\"/>" +
                        "   <address mine=\"asd\">" +
                        "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                        "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                        "   </address>" +
                        "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                        "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH" +
                        "   <gender value=\"FEMALE\"/>" +
                        "   <surname value=\"Gh9g8EvSVo\"/>" +
                        "   <name value=\"Azx1qg8EvSVo\"/>" +
                        "   <charm value=\"4S7UG5gvok\"/>" +
                        "   <workPhone>+7-165-867-45-80</workPhone>" +
                        "   <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                        "   <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                        "</client>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 39: The value of attribute \"id\" associated with an element type \"client\" must not contain the '<' character.\n");
    }

    @Test
    public void parse___ShouldLogError_When_ClientIdWithThreeQuotes() {
        String str = "<client id=\"1-IP3-43-PF-GNsIekwnHR\"\"\">" + // HERE
                        "   <birth value=\"15-12-2002\"/>" +
                        "   <address mine=\"asd\">" +
                        "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                        "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                        "   </address>" +
                        "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                        "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                        "   <gender value=\"FEMALE\"/>" +
                        "   <surname value=\"Gh9g8EvSVo\"/>" +
                        "   <name value=\"Azx1qg8EvSVo\"/>" +
                        "   <charm value=\"4S7UG5gvok\"/>" +
                        "   <workPhone>+7-165-867-45-80</workPhone>" +
                        "   <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                        "   <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                        "</client>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 36: Element type \"client\" must be followed by either attribute specifications, \">\" or \"/>\".\n");
    }

    @Test
    public void parse_ShouldLogError_When_ClientWithoutSplashInClosingTag() {
        String str = "<client id=\"1-IP3-43-PF-GNsIekwnHR\">\n" +
                        "   <birth value=\"15-12-2002\"/>\n" +
                        "   <address mine=\"asd\">\n" +
                        "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>\n" +
                        "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>\n" +
                        "   </address>\n" +
                        "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>\n" +
                        "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH\n</mobilePhone>" +
                        "   <gender value=\"FEMALE\"/>\n" +
                        "   <surname value=\"Gh9g8EvSVo\"/>\n" +
                        "   <name value=\"Azx1qg8EvSVo\"/>\n" +
                        "   <charm value=\"4S7UG5gvok\"/>\n" +
                        "   <workPhone>+7-165-867-45-80</workPhone>\n" +
                        "   <mobilePhone>+7-903-297-09-92</mobilePhone>\n" +
                        "   <patronymic value=\"sn7FcW6YHyhRo\"/>\n" +
                        "<client>"; // HERE

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 16, Column: 9: XML document structures must start and end within the same entity.\n");
    }

    @Test
    public void parse___ShouldIgnoreElement_When_PathIsIncorrect() throws SQLException {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                        "   <birth value=\"15-12-2002\"/>" +
                        "   <address mine=\"asd\">" +
                        "       <surname value=\"Incorrect Surname #1\"/>" + // HERE
                        "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                        "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                        "   </address>" +
                        "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                        "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                        "   <gender value=\"FEMALE\"/>" +
                        "   <hello><world><surname value=\"Incorrect Surname #2\"/></world></hello>" + // HERE
                        "   <name value=\"Azx1qg8EvSVo\"/>" +
                        "   <charm value=\"4S7UG5gvok\"/>" +
                        "   <workPhone>+7-165-867-45-80</workPhone>" +
                        "   <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                        "   <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                        "</client></cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, null);
        //
        //

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM " + tmpClientTable);
            while (resultSetC.next()) {
                String surname = resultSetC.getString("surname");

                assertThat(surname).isNull();
            }
        }
    }

    @Test
    public void parse___ShouldReadElement_When_PathIsCorrect() throws SQLException {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                "   <surname value=\"Correct Surname #1\"/>" + // READ
                "   <birth value=\"15-12-2002\"/>" +
                "   <address mine=\"asd\">" +
                "       <surname value=\"Incorrect Surname #1\"/>" + // NOT READ
                "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                "   </address>" +
                "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "   <gender value=\"FEMALE\"/>" +
                "   <hello><world><surname value=\"Incorrect Surname #2\"/></world></hello>" + // NOT READ
                "   <name value=\"Azx1qg8EvSVo\"/>" +
                "   <charm value=\"4S7UG5gvok\"/>" +
                "   <workPhone>+7-165-867-45-80</workPhone>" +
                "   <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                "   <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "</client></cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, null);
        //
        //

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM " + tmpClientTable);
            while (resultSetC.next()) {
                String surname = resultSetC.getString("surname");

                assertThat(surname).isEqualTo("Correct Surname #1");
            }
        }
    }

    @Test
    public void parse___ShouldIgnoreNestedClient() throws SQLException {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                        "<birth value=\"12-12-2002\"/>" +
                        "<address mine=\"asd\">" +
                        "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                        "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                        "</address>" +
                        "<client id=\"3-ZAC-43-PF-GNsIekwnHR\">" + // HERE
                        "<name value=\"15-12-2002\"/>" +
                        "<workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                        "<gender value=\"FEMALE\"/>" +
                        "<mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                        "<charm value=\"4S7UG5gvok\"/>" +
                        "<workPhone>+7-165-867-45-80</workPhone>" +
                        "<mobilePhone>+7-903-297-09-92</mobilePhone>" +
                        "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                        "</client>" +
                        "<surname value=\"Gh9g8EvSVo\"/>" +
                        "</client></cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, null);
        //
        //

        try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM "+tmpClientTable+" WHERE client_id = '3-ZAC-43-PF-GNsIekwnHR'");
                while (resultSet.next()) {
                    assertThat(resultSet).isNull();
                }
        }
    }

    @Test
    public void parse___ShouldReadMobilePhoneWithNestedCharm() throws SQLException {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                "<birth value=\"2002-12-12\"/>" +
                "<address mine=\"asd\">" +
                "<name value=\"Temilran\"/>" +
                "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                "</address>" +
                "<gender value=\"FEMALE\"/>" +
                "<mobilePhone><charm value=\"4S7UG5gvok\"/>+777083705095</mobilePhone>" + // Charm should not interfere
                "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "<surname value=\"Gh9g8EvSVo\"/>" +
                "</client></cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, null);
        //
        //

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSetP = statement.executeQuery("SELECT * FROM "+tmpPhoneTable);
            while (resultSetP.next()) {
                String number = resultSetP.getString("number");
                assertThat(number).isEqualTo("+777083705095");
            }
        }
    }

    @Test
    public void parse___Should() throws SQLException {
        String str = "<cia><client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                "<birth value=\"2002-12-12\"/>" +
                "<address mine=\"asd\">" +
                "<name value=\"CORRECT_CLIENT_NAME\"/>" +
                "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                "</address>" +
                "<gender value=\"FEMALE\"/>" +
                "<mobilePhone>+777083705095</mobilePhone>" +
                "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "<surname value=\"Gh9g8EvSVo\"/>" +
                "</client>" +
                "<client id=\"2-IP3-43-PF-GNsIekwnHR\">" +
                "<birth value=\"2002-12-12\"/>" +
                "<address mine=\"asd\">" +
                "<name value=\"ERROR_CLIENT_NAME\"/>" +
                "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                "</address>" +
                "<gender value=\"FEMALE\"/>" +
                "<mobilePhone>+777083705095</mobilePhone>" +
                "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "<surname value=\"Gh9g8EvSVo\"/>" +
                "</cia>"; // HERE NO CLOSING CLIENT TAG

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream outputErros = new ByteArrayOutputStream();

        MySAXHandler mySAXHandler = new MySAXHandler(connection, ciaPS, phonesPS);

        //
        //
        mySAXHandler.parse(inputData, outputErros);
        //
        //

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM "+tmpClientTable + " WHERE client_id IN ('1-IP3-43-PF-GNsIekwnHR', '2-IP3-43-PF-GNsIekwnHR')");
            if (resultSet.next()) {
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            }
        }
    }
}
