package kz.greetgo.sandboxserver.migration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static kz.greetgo.sandboxserver.migration.CiaMigration.downloadMaxBatchSize;
import static kz.greetgo.sandboxserver.migration.util.TimeUtils.recordsPerSecond;
import static kz.greetgo.sandboxserver.migration.util.TimeUtils.showTime;

public class MySAXHandler extends DefaultHandler {
    private StringBuilder currentValue;
    private Client currentClient;
    private final PreparedStatement ciaPS;
    private final PreparedStatement phonesPS;
    private final Connection operConnection;
    long startedAt;
    private int batchSize;
    private int phonesBatchSize;
    private int recordsCount;
    private BufferedWriter fatalErrorsWriter;
    private boolean insideNestedClient = false;

    public MySAXHandler(Connection operConnection, PreparedStatement ciaPS, PreparedStatement phonesPS, long startedAt, int recordsCount) {
        this.operConnection = operConnection;
        this.ciaPS = ciaPS;
        this.phonesPS = phonesPS;
        this.startedAt = startedAt;
        this.recordsCount = recordsCount;
        this.batchSize = 0;
        this.phonesBatchSize = 0;
    }

    static class Client {
        String id;
        String surname;
        String name;
        String patronymic;
        String gender;
        String charm;
        String birth;
        String factStreet;
        String factHouse;
        String factFlat;
        String registerStreet;
        String registerHouse;
        String registerFlat;
        List<String> mobilePhones;
        List<String> workPhones;
        String homePhone;
    }

    @Override
    public void startDocument() {
        File file = new File("build/logs/fatal_errors.txt");
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        try {
            fatalErrorsWriter = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentValue = new StringBuilder();
        if (qName.equals("client")) {
            if (currentClient == null) {
                currentClient = new Client();
                currentClient.id = attributes.getValue("id");
                currentClient.mobilePhones = new ArrayList<>();
                currentClient.workPhones = new ArrayList<>();
            } else {
                insideNestedClient = true;
            }
        } else if (attributes.getLength() > 0 && (qName.equals("name") || qName.equals("surname") ||
                qName.equals("patronymic") || qName.equals("gender") || qName.equals("charm") ||
                qName.equals("birth") || qName.equals("homePhone"))) {
            currentValue.append(attributes.getValue("value"));
        } else if (qName.equals("fact") || qName.equals("register")) {
            String street = attributes.getValue("street");
            String house = attributes.getValue("house");
            String flat = attributes.getValue("flat");

            if (qName.equals("fact")) {
                currentClient.factStreet = street;
                currentClient.factHouse = house;
                currentClient.factFlat = flat;
            } else {
                currentClient.registerStreet = street;
                currentClient.registerHouse = house;
                currentClient.registerFlat = flat;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String text = new String(ch, start, length).trim();
        currentValue.append(text);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("client")) {
            if (!insideNestedClient) {
                try {
                    insertRecordToBatch(currentClient);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                currentClient = null;
            }
            insideNestedClient = false;
        } else if (currentClient != null && !insideNestedClient) {
            switch (qName) {
                case "surname":
                    currentClient.surname = currentValue.toString();
                    break;
                case "name":
                    currentClient.name = currentValue.toString();
                    break;
                case "patronymic":
                    currentClient.patronymic = currentValue.toString();
                    break;
                case "gender":
                    currentClient.gender = currentValue.toString();
                    break;
                case "charm":
                    currentClient.charm = currentValue.toString();
                    break;
                case "birth":
                    currentClient.birth = currentValue.toString();
                    break;
                case "homePhone":
                    currentClient.homePhone = currentValue.toString();
                    break;
                case "mobilePhone":
                    currentClient.mobilePhones.add(currentValue.toString());
                    break;
                case "workPhone":
                    currentClient.workPhones.add(currentValue.toString());
                    break;
            }
        }
        currentValue.setLength(0);
    }

    private void addPhones(String type, String value) {
        try {
            phonesPS.setString(1, currentClient.id);
            phonesPS.setString(2, type);
            phonesPS.setString(3, value);
            phonesPS.addBatch();
            phonesBatchSize++;
            if (phonesBatchSize >= downloadMaxBatchSize) {
                phonesPS.executeBatch();
                operConnection.commit();
                phonesBatchSize = 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertRecordToBatch(Client client) throws SQLException {
        ciaPS.setString(1, client.id);
        ciaPS.setString(2, client.surname);
        ciaPS.setString(3, client.name);
        ciaPS.setString(4, client.patronymic);
        ciaPS.setString(5, client.gender);
        ciaPS.setString(6, client.charm);
        ciaPS.setString(7, client.birth);
        ciaPS.setString(8, client.factStreet);
        ciaPS.setString(9, client.factHouse);
        ciaPS.setString(10, client.factFlat);
        ciaPS.setString(11, client.registerStreet);
        ciaPS.setString(12, client.registerHouse);
        ciaPS.setString(13, client.registerFlat);
        ciaPS.addBatch();
        batchSize++;
        recordsCount++;

        if(client.homePhone != null) addPhones("HOME", client.homePhone);
        for (int i = 0; i < client.workPhones.size(); i++) {
            addPhones("WORK", client.workPhones.get(i));
        }
        for (int i = 0; i < client.mobilePhones.size(); i++) {
            addPhones("MOBILE", client.mobilePhones.get(i));
        }

        if (batchSize >= downloadMaxBatchSize) {
            ciaPS.executeBatch();
            operConnection.commit();
            batchSize = 0;
            long now = System.nanoTime();
            System.out.println(" -- downloaded records " + recordsCount + " for " + showTime(now, startedAt)
                    + " : " + recordsPerSecond(recordsCount, now - startedAt));
        }

    }

    @Override
    public void endDocument() {
        try {
            fatalErrorsWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadData();
    }

    private void loadData() {
        try {
            if (batchSize > 0) {
                batchSize = 0;
                ciaPS.executeBatch();
                operConnection.commit();
            }
            if (phonesBatchSize > 0) {
                phonesBatchSize = 0;
                phonesPS.executeBatch();
                operConnection.commit();
            }
            long now = System.nanoTime();
            System.out.println(" -- downloaded records " + recordsCount + " for " + showTime(now, startedAt)
                    + " : " + recordsPerSecond(recordsCount, now - startedAt));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void error(SAXParseException e) {
        logError(e, "Error");
    }

    @Override
    public void fatalError(SAXParseException e) {
        logError(e, "Fatal Error");
        loadData();
    }

    private void logError(SAXParseException e, String severity) {
        int lineNumber = e.getLineNumber();
        int columnNumber = e.getColumnNumber();
        String errorMessage = e.getMessage();
        String errorLocation = "Line: " + lineNumber + ", Column: " + columnNumber + ": ";

        String logMessage = "[" + severity + "] " + errorLocation + errorMessage + "\n";

        try {
            fatalErrorsWriter.write(logMessage);
            fatalErrorsWriter.flush();
            fatalErrorsWriter.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
