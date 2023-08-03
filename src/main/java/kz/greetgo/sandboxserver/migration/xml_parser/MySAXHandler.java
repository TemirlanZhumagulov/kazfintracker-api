//package kz.greetgo.sandboxserver.migration.xml_parser;
//
//import lombok.extern.slf4j.Slf4j;
//import org.xml.sax.Attributes;
//import org.xml.sax.SAXException;
//import org.xml.sax.SAXParseException;
//import org.xml.sax.helpers.DefaultHandler;
//
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Stack;
//
//import static kz.greetgo.sandboxserver.migration.CiaMigration.uploadMaxBatchSize;
//import static kz.greetgo.sandboxserver.migration.util.TimeUtils.recordsPerSecond;
//import static kz.greetgo.sandboxserver.migration.util.TimeUtils.showTime;
//
//@Slf4j
//public class MySAXHandler extends DefaultHandler {
//    public final PreparedStatement ciaPS;
//    public final PreparedStatement phonesPS;
//    public final Connection operConnection;
//    public long startedAt;
//    private OutputStream outputErrors;
//    private int recordsCount;
//    private int batchSize = 0;
//    private int phonesBatchSize = 0;
//    private StringBuilder currentValue;
//    private ClientTmp currentClient;
//    private boolean insidePhoneTag = false;
//
//    private final Stack<String> elementStack = new Stack<>();
//    private final StringBuilder path = new StringBuilder();
//
//    public MySAXHandler(Connection operConnection, PreparedStatement ciaPS, PreparedStatement phonesPS) {
//        this.operConnection = operConnection;
//        this.ciaPS = ciaPS;
//        this.phonesPS = phonesPS;
//        this.recordsCount = 0;
//    }
//
//    public void parse(InputStream inputStream, OutputStream outputStream) {
//        try {
//            this.outputErrors = outputStream;
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            SAXParser saxParser = factory.newSAXParser();
//            saxParser.parse(inputStream, this);
//
//        } catch (SAXException ex) {
//            handleSAXException(ex);
//        } catch (IOException | ParserConfigurationException ex){
//            throw new RuntimeException("67KJDzHKme :: ", ex);
//        }
//    }
//    private void handleSAXException(SAXException ex) {
//        try {
//            outputErrors.write(ex.getMessage().getBytes(StandardCharsets.UTF_8));
//        } catch (IOException e) {
//            throw new RuntimeException("IdP065XL9U :: ", e);
//        }
//    }
//
//    @Override
//    public void startElement(String uri, String localName, String qName, Attributes attributes) {
//        if (qName.equals("client") && path.toString().equals("cia/")) {
//            currentClient = new ClientTmp();
//            currentClient.client_id = attributes.getValue("id");
//            currentClient.mobilePhones = new ArrayList<>();
//            currentClient.workPhones = new ArrayList<>();
//        } else if (attributes.getLength() > 0 && (qName.equals("name") || qName.equals("surname") || qName.equals("patronymic") || qName.equals("gender") || qName.equals("charm") || qName.equals("birth") || qName.equals("homePhone")) && path.toString().equals("cia/client/")) {
//            switch (qName) {
//                case "surname":
//                    currentClient.surname = attributes.getValue("value");
//                    break;
//                case "name":
//                    currentClient.name = attributes.getValue("value");
//                    break;
//                case "patronymic":
//                    currentClient.patronymic = attributes.getValue("value");
//                    break;
//                case "gender":
//                    currentClient.gender = attributes.getValue("value");
//                    break;
//                case "charm":
//                    currentClient.charm = attributes.getValue("value");
//                    break;
//                case "birth":
//                    currentClient.birth = attributes.getValue("value");
//                    break;
//                case "homePhone":
//                    currentClient.homePhone = attributes.getValue("value");
//                    break;
//            }
//        } else if ((qName.equals("fact") || qName.equals("register")) && path.toString().equals("cia/client/address/")) {
//            String street = attributes.getValue("street");
//            String house = attributes.getValue("house");
//            String flat = attributes.getValue("flat");
//
//            if (qName.equals("fact")) {
//                currentClient.factStreet = street;
//                currentClient.factHouse = house;
//                currentClient.factFlat = flat;
//            } else {
//                currentClient.registerStreet = street;
//                currentClient.registerHouse = house;
//                currentClient.registerFlat = flat;
//            }
//        } else if (qName.equals("workPhone") || qName.equals("mobilePhone") && path.toString().equals("cia/client/")) {
//            currentValue = new StringBuilder();
//        }
//        elementStack.push(qName);
//        buildPath();
//    }
//
//    private void buildPath() {
//        path.setLength(0);
//        for (String s : elementStack) {
//            path.append(s).append("/");
//        }
//    }
//
//    @Override
//    public void characters(char[] ch, int start, int length) {
//        if (path.toString().equals("cia/client/workPhone/") || path.toString().equals("cia/client/homePhone/") || path.toString().equals("cia/client/mobilePhone/")) {
//            insidePhoneTag = true;
//            String text = new String(ch, start, length).trim();
//            currentValue.append(text);
//        } else {
//            insidePhoneTag = false;
//        }
//    }
//
//    @Override
//    public void endElement(String uri, String localName, String qName) {
//        if (qName.equals("client") && path.toString().equals("cia/client/")) {
//            try {
//                insertRecordToBatch(currentClient);
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//            currentClient = null;
//        } else if (currentClient != null && insidePhoneTag) {
//            switch (qName) {
//                case "mobilePhone":
//                    currentClient.mobilePhones.add(currentValue.toString());
//                    break;
//                case "workPhone":
//                    currentClient.workPhones.add(currentValue.toString());
//                    break;
//            }
//            insidePhoneTag = false;
//        }
//        elementStack.pop();
//        buildPath();
//    }
//
//    private void addPhones(String type, String value) {
//        try {
//            phonesPS.setString(1, currentClient.client_id);
//            phonesPS.setString(2, type);
//            phonesPS.setString(3, value);
//            phonesPS.addBatch();
//            phonesBatchSize++;
//            if (phonesBatchSize >= uploadMaxBatchSize) {
//                phonesPS.executeBatch();
//                operConnection.commit();
//                phonesBatchSize = 0;
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void insertRecordToBatch(ClientTmp client) throws SQLException {
//        ciaPS.setString(1, client.client_id);
//        ciaPS.setString(2, client.surname);
//        ciaPS.setString(3, client.name);
//        ciaPS.setString(4, client.patronymic);
//        ciaPS.setString(5, client.gender);
//        ciaPS.setString(6, client.charm);
//        ciaPS.setString(7, client.birth);
//        ciaPS.setString(8, client.factStreet);
//        ciaPS.setString(9, client.factHouse);
//        ciaPS.setString(10, client.factFlat);
//        ciaPS.setString(11, client.registerStreet);
//        ciaPS.setString(12, client.registerHouse);
//        ciaPS.setString(13, client.registerFlat);
//        ciaPS.addBatch();
//        batchSize++;
//        recordsCount++;
//
//        if (client.homePhone != null) {
//            addPhones("HOME", client.homePhone);
//        }
//        for (int i = 0; i < client.workPhones.size(); i++) {
//            addPhones("WORK", client.workPhones.get(i));
//        }
//        for (int i = 0; i < client.mobilePhones.size(); i++) {
//            addPhones("MOBILE", client.mobilePhones.get(i));
//        }
//
//        if (batchSize >= uploadMaxBatchSize) {
//            ciaPS.executeBatch();
//            operConnection.commit();
//            batchSize = 0;
//            long now = System.nanoTime();
//            log.info("downloaded records " + recordsCount + " for " + showTime(now, startedAt) + " : " + recordsPerSecond(recordsCount, now - startedAt));
//        }
//
//    }
//
//    @Override
//    public void endDocument() {
//        loadData();
//    }
//
//    private void loadData() {
//        try {
//            if (batchSize > 0) {
//                ciaPS.executeBatch();
//                operConnection.commit();
//                long now = System.nanoTime();
//                recordsCount += batchSize;
//                log.info("downloaded records " + recordsCount + " for " + showTime(now, startedAt) + " : " + recordsPerSecond(recordsCount, now - startedAt));
//                batchSize = 0;
//            }
//            if (phonesBatchSize > 0) {
//                phonesPS.executeBatch();
//                operConnection.commit();
//                phonesBatchSize = 0;
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void error(SAXParseException e) {
//        logError(e, "Error");
//    }
//
//    @Override
//    public void fatalError(SAXParseException e) {
//        logError(e, "Fatal Error");
//        loadData();
//    }
//
//    private void logError(SAXParseException e, String severity) {
//        int lineNumber = e.getLineNumber();
//        int columnNumber = e.getColumnNumber();
//        String errorMessage = e.getMessage();
//        String errorLocation = "Line: " + lineNumber + ", Column: " + columnNumber + ": ";
//
//        String logMessage = "[" + severity + "] " + errorLocation + errorMessage + "\n";
//
//        try {
//            outputErrors.write(logMessage.getBytes(StandardCharsets.UTF_8));
//        } catch (IOException ex) {
//            throw new RuntimeException("LBOMbl5O2s :: ", ex);
//        }
//    }
//}
