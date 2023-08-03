package kz.greetgo.sandboxserver.migration.xml_parser;

import kz.greetgo.sandboxserver.migration.DatabaseAccess;
import kz.greetgo.sandboxserver.migration.DatabaseSetup;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class XmlParserTest {
    private Connection connection;
    private PreparedStatement ciaPS;
    private PreparedStatement phonesPS;
    private DatabaseAccess dbAccess;
    private String tmpPhoneTable;

    @BeforeMethod
    public void setUpXmlParser() {
        connection = DatabaseSetup.getConnection();

        // Create unique table names for each test
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        Date nowDate = new Date();
        String tmpClientTable = "tmp_" + sdf.format(nowDate) + "_client";
        tmpPhoneTable = "tmp_" + sdf.format(nowDate) + "_phone";

        // Create tables
        DatabaseSetup.createCiaMigrationTmpTables(tmpClientTable, tmpPhoneTable);

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

        // Create dbAccess
        dbAccess = new DatabaseAccess(connection);
        dbAccess.tmpClientTable = tmpClientTable;
        dbAccess.tmpPhoneTable = tmpPhoneTable;
    }

    @AfterMethod
    public void closeSaxHandlerConnections() {
        try {
            if (ciaPS != null) {
                ciaPS.close();
                ciaPS = null;
            }
            if (phonesPS != null) {
                phonesPS.close();
                phonesPS = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void assertPhoneExists(String phoneNumber, String type) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + tmpPhoneTable + " WHERE type = '" + type + "' AND number = '" + phoneNumber + "'")) {
            if (resultSet.next()) {
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            } else {
                throw new AssertionError("Phone with type " + type + " and number " + phoneNumber + " not found in " + tmpPhoneTable);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void parse___ShouldLoad_XMLClientData_ToTmpTables() throws SQLException {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe32kwnHR\">" +
                "       <surname value=\"s23as32sw123\"/>" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <name value=\"na32sd123\"/>" +
                "       <address>" +
                "           <fact street=\"1CbPis8PMcGfcBSTQza4p\" house=\"s5\" flat=\"Di\"/>" +
                "           <register street=\"Fh5PGzqIoxXK6r8Jg3zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "       <homePhone value=\"+7-718-096-63-20\"/>" +
                "       <charm value=\"4S7UG5gv32ok\"/>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "</cia>";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);


        //
        //
        XmlParser.parse(byteArrayInputStream, null);
        //
        //

        Map<String, String> map = dbAccess.getTmpClientByClientId("1-IP3-43-PF-GNsIe32kwnHR");

        assertThat(map.get("client_id")).isEqualTo("1-IP3-43-PF-GNsIe32kwnHR");
        assertThat(map.get("surname")).isEqualTo("s23as32sw123");
        assertThat(map.get("birth")).isEqualTo("12-12-2002");
        assertThat(map.get("name")).isEqualTo("na32sd123");
        assertThat(map.get("fact_street")).isEqualTo("1CbPis8PMcGfcBSTQza4p");
        assertThat(map.get("register_street")).isEqualTo("Fh5PGzqIoxXK6r8Jg3zqH");
        assertThat(map.get("fact_house")).isEqualTo("s5");
        assertThat(map.get("register_house")).isEqualTo("Jq");
        assertThat(map.get("fact_flat")).isEqualTo("Di");
        assertThat(map.get("register_flat")).isEqualTo("ta");
        assertThat(map.get("charm")).isEqualTo("4S7UG5gv32ok");
        assertThat(map.get("patronymic")).isEqualTo("sn7FcW6YHyhRo");
        assertThat(map.get("gender")).isEqualTo("FEMALE");
        assertPhoneExists("+7-165-867-45-80", "WORK");
        assertPhoneExists("+7-718-096-63-80 вн. Y2RH", "MOBILE");
        assertPhoneExists("+7-718-096-63-20", "HOME");
    }

    @Test
    public void parse___ShouldLogError_When_MobilePhoneWithoutMatchingEndTag() {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <surname value=\"s23as32sw123\"/>" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <name value=\"na32sd123\"/>" +
                "       <address>" +
                "           <fact street=\"1CbPis8PMcGfcBSTQza4p\" house=\"s5\" flat=\"Di\"/>" +
                "           <register street=\"Fh5PGzqIoxXK6r8Jg3zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-718-096-63-80 вн. Y2RH" + // Without closing mobile phone tag
                "       <homePhone value=\"+7-718-096-63-20\"/>" +
                "       <charm value=\"4S7UG5gv32ok\"/>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();

        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 577: The element type \"mobilePhone\" must be terminated by the matching end-tag \"</mobilePhone>\".\n");
    }

    @Test
    public void parse___shouldLogError_When_NotWellFormedDocument() {
        String str = "" +
                "<cia>" +
                "   <<client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">>" + // Here
                "       <surname value=\"s23as32sw123\"/>" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <name value=\"na32sd123\"/>" +
                "       <address>" +
                "           <fact street=\"1CbPis8PMcGfcBSTQza4p\" house=\"s5\" flat=\"Di\"/>" +
                "           <register street=\"Fh5PGzqIoxXK6r8Jg3zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-718-096-63-80 вн. Y2RH" +
                "       <homePhone value=\"+7-718-096-63-20\"/>" +
                "       <charm value=\"4S7UG5gv32ok\"/>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 10: The content of elements must consist of well-formed character data or markup.\n");
    }

    @Test
    public void parse___ShouldLogError_When_ClientIdQuoteIsNotClosed() {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR>" + // Here
                "       <surname value=\"s23as32sw123\"/>" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <name value=\"na32sd123\"/>" +
                "       <address>" +
                "           <fact street=\"1CbPis8PMcGfcBSTQza4p\" house=\"s5\" flat=\"Di\"/>" +
                "           <register street=\"Fh5PGzqIoxXK6r8Jg3zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "       <homePhone value=\"+7-718-096-63-20\"/>" +
                "       <charm value=\"4S7UG5gv32ok\"/>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 53: The value of attribute \"id\" associated with an element type \"client\" must not contain the '<' character.\n");
    }

    @Test
    public void parse___ShouldLogError_When_ClientIdWithThreeQuotes() {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\"\"\">" + // Here
                "       <surname value=\"s23as32sw123\"/>" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <name value=\"na32sd123\"/>" +
                "       <address>" +
                "           <fact street=\"1CbPis8PMcGfcBSTQza4p\" house=\"s5\" flat=\"Di\"/>" +
                "           <register street=\"Fh5PGzqIoxXK6r8Jg3zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "       <homePhone value=\"+7-718-096-63-20\"/>" +
                "       <charm value=\"4S7UG5gv32ok\"/>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 46: Element type \"client\" must be followed by either attribute specifications, \">\" or \"/>\".\n");
    }

    @Test
    public void parse_ShouldLogError_When_ClientWithoutSplashInClosingTag() {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <surname value=\"s23as32sw123\"/>" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <name value=\"na32sd123\"/>" +
                "       <address>" +
                "           <fact street=\"1CbPis8PMcGfcBSTQza4p\" house=\"s5\" flat=\"Di\"/>" +
                "           <register street=\"Fh5PGzqIoxXK6r8Jg3zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "       <homePhone value=\"+7-718-096-63-20\"/>" +
                "       <charm value=\"4S7UG5gv32ok\"/>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   <client>" + // Here
                "</cia>";
        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, outputErrors);
        //
        //

        String errors = outputErrors.toString();
        assertThat(errors).isEqualTo("[Fatal Error] Line: 1, Column: 599: The element type \"client\" must be terminated by the matching end-tag \"</client>\".\n");
    }

    @Test
    public void parse___ShouldIgnoreElement_When_PathIsIncorrect() throws SQLException {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <birth value=\"15-12-2002\"/>" +
                "       <address mine=\"asd\">" +
                "           <surname value=\"Incorrect Surname #1\"/>" + // HERE
                "           <fact mine=\"asd\" street=\"1CbPis8PMcGfcBST\" house=\"s5\" flat=\"Di\"/>" +
                "           <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                "       <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <hello><world><surname value=\"Incorrect Surname #2\"/></world></hello>" + // HERE
                "       <name value=\"Azx1qg8EvSVo\"/>" +
                "       <charm value=\"4S7UG5gvk\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "</cia>";
        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, null);
        //
        //

        Map<String, String> client = dbAccess.getTmpClientByClientId("1-IP3-43-PF-GNsIe3kw2nHR");
        assertThat(client.get("surname")).isNull();
    }

    @Test
    public void parse___ShouldReadElement_When_PathIsCorrect() throws SQLException {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <surname value=\"Correct Surname #1\"/>" + // Read
                "       <birth value=\"15-12-2002\"/>" +
                "       <address mine=\"asd\">" +
                "           <surname value=\"Incorrect Surname #1\"/>" +
                "           <fact mine=\"asd\" street=\"1CbPis8PMcGfcBST\" house=\"s5\" flat=\"Di\"/>" +
                "           <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                "       <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <hello><world><surname value=\"Incorrect Surname #2\"/></world></hello>" +
                "       <name value=\"Azx1qg8EvSVo\"/>" +
                "       <charm value=\"4S7UG5gd\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, null);
        //
        //

        Map<String, String> client = dbAccess.getTmpClientByClientId("1-IP3-43-PF-GNsIe3kw2nHR");
        assertThat(client.get("surname")).isEqualTo("Correct Surname #1");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void parse___ShouldCheck_ThereIsNoNestedClientsInTmp() throws SQLException {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <address mine=\"asd\">" +
                "           <fact mine=\"asd\" street=\"1CbPis8PMcGfcBST\" house=\"s5\" flat=\"Di\"/>" +
                "           <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8Jg\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <client id=\"3-ZAC-43-PF-GNsIe3kw2nHR\">" + // HERE
                "           <name value=\"15-12-2002\"/>" +
                "           <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                "           <gender value=\"FEMALE\"/>" +
                "           <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "           <charm value=\"4S7UG5g\"/>" +
                "           <workPhone>+7-165-867-45-80</workPhone>" +
                "           <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                "           <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "       </client>" +
                "       <surname value=\"Gh9g8EvSVo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, null);
        //
        //

        dbAccess.getTmpClientByClientId("3-ZAC-43-PF-GNsIe3kw2nHR");
    }

    @Test
    public void parse___ShouldCheck_NestedClientsShouldNotInterfere() throws SQLException {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <address mine=\"asd\">" +
                "           <fact mine=\"asd\" street=\"1CbPis8PMcGfcBST\" house=\"s5\" flat=\"Di\"/>" +
                "           <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8Jg\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <client id=\"3-ZAC-43-PF-GNsIe3kw2nHR\">" + // HERE
                "           <name value=\"15-12-2002\"/>" +
                "           <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                "           <gender value=\"FEMALE\"/>" +
                "           <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "           <charm value=\"4S7UG5g\"/>" +
                "           <workPhone>+7-165-867-45-80</workPhone>" +
                "           <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                "           <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "       </client>" +
                "       <surname value=\"Gh9g8EvSVo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, null);
        //
        //

        Map<String, String> client = dbAccess.getTmpClientByClientId("1-IP3-43-PF-GNsIe3kw2nHR");

        assertThat(client.get("client_id")).isEqualTo("1-IP3-43-PF-GNsIe3kw2nHR");
        assertThat(client.get("surname")).isEqualTo("Gh9g8EvSVo");
        assertThat(client.get("birth")).isEqualTo("12-12-2002");
        assertThat(client.get("name")).isNull();
        assertThat(client.get("fact_street")).isEqualTo("1CbPis8PMcGfcBST");
        assertThat(client.get("register_street")).isEqualTo("Fh5PGzqIoxXK6r8Jg");
        assertThat(client.get("fact_house")).isEqualTo("s5");
        assertThat(client.get("register_house")).isEqualTo("Jq");
        assertThat(client.get("fact_flat")).isEqualTo("Di");
        assertThat(client.get("register_flat")).isEqualTo("ta");
        assertThat(client.get("charm")).isNull();
        assertThat(client.get("patronymic")).isNull();
        assertThat(client.get("gender")).isNull();
    }

    @Test
    public void parse___ShouldReadMobilePhoneWithNestedCharm() {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <birth value=\"2002-12-12\"/>" +
                "       <address mine=\"asd\">" +
                "           <name value=\"qwe2asd\"/>" +
                "           <fact mine=\"asd\" street=\"1CbPis8PMcGfcBST\" house=\"s5\" flat=\"Di\"/>" +
                "           <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <mobilePhone><charm value=\"4S7UG5gvk\"/>+777083705095</mobilePhone>" + // Charm should not interfere
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "       <surname value=\"Gh9g8EvSVo\"/>" +
                "   </client>" +
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, null);
        //
        //

        assertPhoneExists("+777083705095", "MOBILE");
    }

    @Test
    public void parse___ShouldLoadCorrectClients_BeforeFatalErrorOccurs() throws SQLException {
        String str = "" +
                "<cia>" +
                "   <client id=\"1-IP3-43-PF-GNsIe32kwnHR\">" +
                "       <surname value=\"s23as32sw123\"/>" +
                "       <birth value=\"12-12-2002\"/>" +
                "       <name value=\"na32sd123\"/>" +
                "       <address>" +
                "           <fact street=\"1CbPis8PMcGfcBSTQza4p\" house=\"s5\" flat=\"Di\"/>" +
                "           <register street=\"Fh5PGzqIoxXK6r8Jg3zqH\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <workPhone>+7-165-867-45-80</workPhone>" +
                "       <mobilePhone>+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                "       <homePhone value=\"+7-718-096-63-20\"/>" +
                "       <charm value=\"4S7UG5gv32ok\"/>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "   </client>" +
                "   <client id=\"2-IP3-43-PF-GNsIe3kw2nHR\">" +
                "       <birth value=\"2002-12-12\"/>" +
                "       <address mine=\"asd\">" +
                "           <name value=\"ERROR_CLIENT_NAME\"/>" +
                "           <fact mine=\"asd\" street=\"1CbPis8PMcGfcBST\" house=\"s5\" flat=\"Di\"/>" +
                "           <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8\" house=\"Jq\" flat=\"ta\"/>" +
                "       </address>" +
                "       <gender value=\"FEMALE\"/>" +
                "       <mobilePhone>+777083705095</mobilePhone>" +
                "       <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                "       <surname value=\"Gh9g8EvSVo\"/>" +
                // No closing client tag here
                "</cia>";

        ByteArrayInputStream inputData = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream outputErrors = new ByteArrayOutputStream();

        XmlParser XmlParser = new XmlParser(connection, ciaPS, phonesPS);

        //
        //
        XmlParser.parse(inputData, outputErrors);
        //
        //

        Map<String, String> map = dbAccess.getTmpClientByClientId("1-IP3-43-PF-GNsIe32kwnHR");

        assertThat(map.get("client_id")).isEqualTo("1-IP3-43-PF-GNsIe32kwnHR");
        assertThat(map.get("surname")).isEqualTo("s23as32sw123");
        assertThat(map.get("birth")).isEqualTo("12-12-2002");
        assertThat(map.get("name")).isEqualTo("na32sd123");
        assertThat(map.get("fact_street")).isEqualTo("1CbPis8PMcGfcBSTQza4p");
        assertThat(map.get("register_street")).isEqualTo("Fh5PGzqIoxXK6r8Jg3zqH");
        assertThat(map.get("fact_house")).isEqualTo("s5");
        assertThat(map.get("register_house")).isEqualTo("Jq");
        assertThat(map.get("fact_flat")).isEqualTo("Di");
        assertThat(map.get("register_flat")).isEqualTo("ta");
        assertThat(map.get("charm")).isEqualTo("4S7UG5gv32ok");
        assertThat(map.get("patronymic")).isEqualTo("sn7FcW6YHyhRo");
        assertThat(map.get("gender")).isEqualTo("FEMALE");
        assertPhoneExists("+7-165-867-45-80", "WORK");
        assertPhoneExists("+7-718-096-63-80 вн. Y2RH", "MOBILE");
        assertPhoneExists("+7-718-096-63-20", "HOME");
    }
}
