package kz.greetgo.sandboxserver.migration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;

public class MySAXHandlerTest {

    private void downloadXmlString(String xmlString) throws SAXException {
        String insertClientSQL = "INSERT INTO client_tmp (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO phone_tmp (client_id, type, number) VALUES (?,?,?)";
        try (Connection connection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
             PreparedStatement ciaPS = connection.prepareStatement(insertClientSQL);
             PreparedStatement phonesPS = connection.prepareStatement(insertPhonesPS)) {
            long startedAt = System.nanoTime();
            int recordsCount = 0;

            MySAXHandler saxHandler = new MySAXHandler(connection, ciaPS, phonesPS, startedAt, recordsCount);

            connection.setAutoCommit(false);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputSource inputSource = new InputSource(new StringReader(xmlString));
            saxParser.parse(inputSource, saxHandler);

        } catch (IOException | SQLException | ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void shouldReadAllFields(){
        try {
            String sb = "<client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
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
                    "</client>";
            downloadXmlString(sb);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        try (Connection operConnection = DatabaseSetup.getConnection();
             Statement statement = operConnection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client_tmp");
            while (resultSet.next()) {
                String surname = resultSet.getString("surname");
                Assertions.assertThat("s").isEqualTo(surname);

                String birth = resultSet.getString("birth");
                Assertions.assertThat("12-12-2002").isEqualTo(birth);

                String name = resultSet.getString("name");
                Assertions.assertThat("n").isEqualTo(name);

                String factStreet = resultSet.getString("fact_street");
                Assertions.assertThat("1CbPis8PMcGfcBSTQzap").isEqualTo(factStreet);

                String regStreet = resultSet.getString("register_street");
                Assertions.assertThat("Fh5PGzqIoxXK6r8JgzqH").isEqualTo(regStreet);

                String facHouse = resultSet.getString("fact_house");
                Assertions.assertThat("s5").isEqualTo(facHouse);

                String regHouse = resultSet.getString("register_house");
                Assertions.assertThat("Jq").isEqualTo(regHouse);

                String facFlat = resultSet.getString("fact_flat");
                Assertions.assertThat("Di").isEqualTo(facFlat);

                String regFlat = resultSet.getString("register_flat");
                Assertions.assertThat("ta").isEqualTo(regFlat);

                String charm = resultSet.getString("charm");
                Assertions.assertThat("4S7UG5gvok").isEqualTo(charm);

                String patronymic = resultSet.getString("patronymic");
                Assertions.assertThat("sn7FcW6YHyhRo").isEqualTo(patronymic);

                String gender = resultSet.getString("gender");
                Assertions.assertThat("FEMALE").isEqualTo(gender);
            }
            ResultSet resultSet2 = statement.executeQuery("SELECT * FROM phone_tmp");
            StringBuilder types = new StringBuilder();
            StringBuilder phones = new StringBuilder();
            while(resultSet2.next()){
                phones.append(resultSet2.getString("number"));
                types.append(resultSet2.getString("type"));
            }
            Assertions.assertThat(types.toString()).isEqualTo("HOMEWORKMOBILE");
            Assertions.assertThat(phones.toString()).isEqualTo("+7-718-096-63-20+7-165-867-45-80+7-718-096-63-80 вн. Y2RH");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void mobilePhoneTagIsNotClosed() {
        try {
            downloadXmlString(
                    "<client id=\"1-IP3-43-PF-GNsIekwnHR\">\n" +
                            "   <birth value=\"15-12-2002\"/>\n" +
                            "   <address mine=\"asd\">\n" +
                            "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>\n" +
                            "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>\n" +
                            "   </address>\n" +
                            "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>\n" +
                            "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH\n" + // HERE
                            "   <gender value=\"FEMALE\"/>\n" +
                            "   <surname value=\"Gh9g8EvSVo\"/>\n" +
                            "   <name value=\"Azx1qg8EvSVo\"/>\n" +
                            "   <charm value=\"4S7UG5gvok\"/>\n" +
                            "   <workPhone>+7-165-867-45-80</workPhone>\n" +
                            "   <mobilePhone>+7-903-297-09-92</mobilePhone>\n" +
                            "   <patronymic value=\"sn7FcW6YHyhRo\"/>\n" +
                            "</client>");
        } catch (SAXException e) {
            try (BufferedReader brTest = new BufferedReader(new FileReader("build/logs/fatal_errors.txt"))) {
                String text = brTest.readLine();
                String error = "[Fatal Error] Line: 16, Column: 3: The element type \"mobilePhone\" must be terminated by the matching end-tag \"</mobilePhone>\".";
                Assertions.assertThat(text).isEqualTo(error);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    @Test
    public void clientWithDoubleAngleBracket() {
        try {
            downloadXmlString(
                    "<<client id=\"1-IP3-43-PF-GNsIekwnHR\">>\n" + // HERE
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
                            "</client>");
        } catch (SAXException e) {
            try (BufferedReader brTest = new BufferedReader(new FileReader("build/logs/fatal_errors.txt"))) {
                String text = brTest.readLine();
                String error = "[Fatal Error] Line: 1, Column: 2: The markup in the document preceding the root element must be well-formed.";
                Assertions.assertThat(text).isEqualTo(error);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    @Test
    public void clientIdQuoteIsNotClosed() {
        try {
            downloadXmlString(
                    "<client id=\"1-IP3-43-PF-GNsIekwnHR>" + // HERE
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
                            "</client>");
        } catch (SAXException e) {
            try (BufferedReader brTest = new BufferedReader(new FileReader("build/logs/fatal_errors.txt"))) {
                String text = brTest.readLine();
                String error = "[Fatal Error] Line: 1, Column: 39: The value of attribute \"id\" associated with an element type \"client\" must not contain the '<' character.";
                Assertions.assertThat(text).isEqualTo(error);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    @Test
    public void clientIdWithThreeQuotes() {
        try {
            downloadXmlString(
                    "<client id=\"1-IP3-43-PF-GNsIekwnHR\"\"\">" + // HERE
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
                            "</client>");
        } catch (SAXException e) {
            try (BufferedReader brTest = new BufferedReader(new FileReader("build/logs/fatal_errors.txt"))) {
                String text = brTest.readLine();
                String error = "[Fatal Error] Line: 1, Column: 36: Element type \"client\" must be followed by either attribute specifications, \">\" or \"/>\".";
                Assertions.assertThat(text).isEqualTo(error);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    @Test
    public void clientWithoutSplashInClosingTag() {
        try {
            downloadXmlString(
                    "<client id=\"1-IP3-43-PF-GNsIekwnHR\">\n" +
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
                            "<client>"); // HERE
        } catch (SAXException e) {
            try (BufferedReader brTest = new BufferedReader(new FileReader("build/logs/fatal_errors.txt"))) {
                String text = brTest.readLine();
                String error = "[Fatal Error] Line: 16, Column: 9: XML document structures must start and end within the same entity.";
                Assertions.assertThat(text).isEqualTo(error);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void additionalTagsShouldNotInterfere() {
        try {
            downloadXmlString(
                    "<client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                            "   <birth value=\"15-12-2002\"/>" +
                            "   <address mine=\"asd\">" +
                            "       <fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                            "       <register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                            "   </address>" +
                            "   <workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                            "   <mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                            "   <gender value=\"FEMALE\"/>" +
                            "   <hello><world><surname value=\"Gh9g8EvSVo\"/></world></hello>" + // HERE
                            "   <name value=\"Azx1qg8EvSVo\"/>" +
                            "   <charm value=\"4S7UG5gvok\"/>" +
                            "   <workPhone>+7-165-867-45-80</workPhone>" +
                            "   <mobilePhone>+7-903-297-09-92</mobilePhone>" +
                            "   <patronymic value=\"sn7FcW6YHyhRo\"/>" +
                            "</client>");
        } catch (SAXException e) {
            e.printStackTrace();
        }
        try (Connection operConnection = DatabaseSetup.getConnection();
             Statement statement = operConnection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp");
            while (resultSetC.next()) {
                String surname = resultSetC.getString("surname");
                Assertions.assertThat("Gh9g8EvSVo").isEqualTo(surname);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void shouldReadLastTagsWhenDuplicates() {
        try {
            String sb = "<client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                    "<birth value=\"1-1-2002\"/>" +
                    "<surname value=\"Surname #1\"/>" +
                    "<name value=\"Temilran\"/>" +
                    "<address mine=\"asd\">" +
                    "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                    "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                    "</address>" +
                    "<gender value=\"FEMALE\"/>" +
                    "<workPhone>+7-165-867-45-80</workPhone>" +
                    "<workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                    "<mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                    "<mobilePhone><surname value=\"Gh9g8EvSVo\"/></mobilePhone>" +
                    "<charm value=\"4S7UG5gvok\"/>" +
                    "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                    "<birth value=\"12-12-2002\"/>" +
                    "<name value=\"Name #1\"/>" +
                    "</client>";
            downloadXmlString(sb);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        try (Connection operConnection = DatabaseSetup.getConnection();
             Statement statement = operConnection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp");
            while (resultSetC.next()) {
                String surname = resultSetC.getString("surname");
                String birth = resultSetC.getString("birth");
                String name = resultSetC.getString("name");
                Assertions.assertThat("Gh9g8EvSVo").isEqualTo(surname);
                Assertions.assertThat("12-12-2002").isEqualTo(birth);
                Assertions.assertThat("Name #1").isEqualTo(name);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void tagCannotBeClosedByAnotherTag() {
        try {
            String sb = "<client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                    "<birth value=\"12-12-2002\"/>" +
                    "<name value=\"Temilran\"/>" +
                    "<address mine=\"asd\">" +
                    "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                    "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                    "</address>" +
                    "<workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                    "<gender value=\"FEMALE\"/>" +
                    "<mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                    "<charm value=\"4S7UG5gvok\"/>" +
                    "<mobilePhone>+7-165-867-45-80<workPhone>+7-165-867-45-80</mobilePhone></workPhone>" + // HERE
                    "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                    "</client>";
            downloadXmlString(sb);
        } catch (SAXException e) {
            try (BufferedReader brTest = new BufferedReader(new FileReader("build/logs/fatal_errors.txt"))) {
                String text = brTest.readLine();
                String error = "[Fatal Error] Line: 1, Column: 482: The element type \"workPhone\" must be terminated by the matching end-tag \"</workPhone>\".";
                Assertions.assertThat(text).isEqualTo(error);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void ignoreNestedClientInsideClient() {
        try {
            downloadXmlString(
                    "<client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                    "<birth value=\"12-12-2002\"/>" +
                    "<address mine=\"asd\">" +
                    "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                    "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                    "</address>" +
                    "<client id=\"3-ZAC-43-PF-GNsIekwnHR\">" + // HERE
                    "<name value=\"15-12-2002\"/>" + // The name should be ignored and equal to null
                    "<workPhone mine=\"asd\">+7-606-415-31-26</workPhone>" +
                    "<gender value=\"FEMALE\"/>" +
                    "<mobilePhone mine=\"asd\">+7-718-096-63-80 вн. Y2RH</mobilePhone>" +
                    "<charm value=\"4S7UG5gvok\"/>" +
                    "<workPhone>+7-165-867-45-80</workPhone>" +
                    "<mobilePhone>+7-903-297-09-92</mobilePhone>" +
                    "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                    "</client>" +
                    "<surname value=\"Gh9g8EvSVo\"/>" +
                    "</client>"
            );
        } catch (SAXException e) {
            e.printStackTrace();
        }

        try (Connection operConnection = DatabaseSetup.dropCreateTables("client_tmp", "phone_tmp");
             Statement statement = operConnection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client_tmp WHERE client_id = '3-ZAC-43-PF-GNsIekwnHR'");
            while (resultSet.next()) {
                Assertions.assertThat(resultSet).isNull();
            }
            ResultSet resultSet2 = statement.executeQuery("SELECT * FROM client_tmp WHERE client_id = '1-IP3-43-PF-GNsIekwnHR'");
            while (resultSet2.next()) {
                String name = resultSet2.getString("name");
                Assertions.assertThat(name).isNull();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldReadMobilePhoneWithNestedCharm() {
        try {
            String sb = "<client id=\"1-IP3-43-PF-GNsIekwnHR\">" +
                    "<birth value=\"2002-12-12\"/>" +
                    "<address mine=\"asd\">" +
                    "<name value=\"Temilran\"/>" +
                    "<fact mine=\"asd\" street=\"1CbPis8PMcGfcBSTQzap\" house=\"s5\" flat=\"Di\"/>" +
                    "<register fine=\"asd\" street=\"Fh5PGzqIoxXK6r8JgzqH\" house=\"Jq\" flat=\"ta\"/>" +
                    "</address>" +
                    "<gender value=\"FEMALE\"/>" +
                    "<mobilePhone><charm value=\"4S7UG5gvok\"/>+777083705095</mobilePhone>" + // Should not interfere
                    "<patronymic value=\"sn7FcW6YHyhRo\"/>" +
                    "<surname value=\"Gh9g8EvSVo\"/>" +
                    "</client>";
            downloadXmlString(sb);
        } catch (SAXException e) {
            e.printStackTrace();
        }

        try (Connection operConnection = DatabaseSetup.getConnection();
             Statement statement = operConnection.createStatement()) {
            //language=PostgreSQL
            ResultSet resultSetC = statement.executeQuery("SELECT * FROM client_tmp");
            while (resultSetC.next()) {
                String charm = resultSetC.getString("charm");
                Assertions.assertThat(charm).isEqualTo("4S7UG5gvok");
            }
            ResultSet resultSetP = statement.executeQuery("SELECT * FROM phone_tmp");
            while (resultSetP.next()) {
                String number = resultSetP.getString("number");
                Assertions.assertThat(number).isEqualTo("+777083705095");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
