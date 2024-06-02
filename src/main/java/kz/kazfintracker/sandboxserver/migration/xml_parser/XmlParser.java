package kz.kazfintracker.sandboxserver.migration.xml_parser;

import kz.kazfintracker.sandboxserver.migration.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static kz.kazfintracker.sandboxserver.migration.CiaMigration.uploadMaxBatchSize;

@Slf4j
public class XmlParser extends SaxHandler {
    public final PreparedStatement ciaPS;
    public final PreparedStatement phonesPS;
    public final Connection connection;
    public long startedAt;
    private OutputStream outputErrors;
    private int recordsCount;
    private int batchSize = 0;
    private int phonesBatchSize = 0;
    private ClientTmp currentClient;

    public XmlParser(Connection connection, PreparedStatement ciaPS, PreparedStatement phonesPS) {
        this.connection = connection;
        this.ciaPS = ciaPS;
        this.phonesPS = phonesPS;
    }

    public void parse(InputStream inputStream, OutputStream outputStream) {
        try {
            this.outputErrors = outputStream;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse(inputStream, this);
        } catch (SAXException ignored) { // This exception is handled in @Override fatalError method
        } catch (IOException | ParserConfigurationException ex) {
            throw new RuntimeException("67KJDzHKme :: ", ex);
        }
    }

    @Override
    protected void startTag(Attributes attributes) {
        String path = path();
        if ("/cia/client".equals(path)) {
            currentClient = new ClientTmp();
            currentClient.client_id = attributes.getValue("id");
            currentClient.mobilePhones = new ArrayList<>();
            currentClient.workPhones = new ArrayList<>();
            return;
        }
        if ("/cia/client/surname".equals(path)) {
            currentClient.surname = attributes.getValue("value");
            return;
        }
        if ("/cia/client/name".equals(path)) {
            currentClient.name = attributes.getValue("value");
            return;
        }
        if ("/cia/client/birth".equals(path)) {
            currentClient.birth = attributes.getValue("value");
            return;
        }
        if ("/cia/client/patronymic".equals(path)) {
            currentClient.patronymic = attributes.getValue("value");
            return;
        }
        if ("/cia/client/charm".equals(path)) {
            currentClient.charm = attributes.getValue("value");
            return;
        }
        if ("/cia/client/gender".equals(path)) {
            currentClient.gender = attributes.getValue("value");
            return;
        }
        if ("/cia/client/homePhone".equals(path)) {
            currentClient.homePhone = attributes.getValue("value");
        }
        if ("/cia/client/address/fact".equals(path)) {
            currentClient.factStreet = attributes.getValue("street");
            currentClient.factHouse = attributes.getValue("house");
            currentClient.factFlat = attributes.getValue("flat");
            return;
        }
        if ("/cia/client/address/register".equals(path)) {
            currentClient.registerStreet = attributes.getValue("street");
            currentClient.registerHouse = attributes.getValue("house");
            currentClient.registerFlat = attributes.getValue("flat");
        }
    }

    @Override
    protected void endTag() {
        String path = path();
        if ("/cia/client/mobilePhone".equals(path)) {
            currentClient.mobilePhones.add(text());
            return;
        }
        if ("/cia/client/workPhone".equals(path)) {
            currentClient.workPhones.add(text());
            return;
        }
        if ("/cia/client".equals(path)) {
            insertClientToBatch(currentClient);
        }
    }

    private void insertClientToBatch(ClientTmp client) {
        try {
            ciaPS.setString(1, client.client_id);
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

            for (int i = 0; i < client.workPhones.size(); i++) {
                insertPhoneToPhoneBatch("WORK", client.workPhones.get(i));
            }
            for (int i = 0; i < client.mobilePhones.size(); i++) {
                insertPhoneToPhoneBatch("MOBILE", client.mobilePhones.get(i));
            }
            if (client.homePhone != null) {
                insertPhoneToPhoneBatch("HOME", client.homePhone);
            }

            if (batchSize >= uploadMaxBatchSize) {
                ciaPS.executeBatch();
                connection.commit();
                batchSize = 0;
                long now = System.nanoTime();
                log.info("avUOTsGR9P :: downloaded records " + recordsCount + " for " + TimeUtils.showTime(now, startedAt) + " : " + TimeUtils.recordsPerSecond(recordsCount, now - startedAt));
            }
        } catch (SQLException e) {
            throw new RuntimeException("O2i80Bi6Fv :: ", e);
        }
    }

    private void insertPhoneToPhoneBatch(String type, String value) {
        try {
            phonesPS.setString(1, currentClient.client_id);
            phonesPS.setString(2, type);
            phonesPS.setString(3, value);
            phonesPS.addBatch();
            phonesBatchSize++;
            if (phonesBatchSize >= uploadMaxBatchSize) {
                phonesPS.executeBatch();
                connection.commit();
                phonesBatchSize = 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("3P4wHLyZqw :: ", e);
        }
    }

    @Override
    public void endDocument() {
        loadData();
    }

    private void loadData() {
        try {
            if (batchSize > 0) {
                ciaPS.executeBatch();
                connection.commit();
                long now = System.nanoTime();
                recordsCount += batchSize;
                log.info("XDP0ux4d2p :: downloaded records " + recordsCount + " for " + TimeUtils.showTime(now, startedAt) + " : " + TimeUtils.recordsPerSecond(recordsCount, now - startedAt));
                batchSize = 0;
            }
            if (phonesBatchSize > 0) {
                phonesPS.executeBatch();
                connection.commit();
                phonesBatchSize = 0;
            }
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
        String errorLocation = "Line: " + e.getLineNumber() + ", Column: " + e.getColumnNumber() + ": ";
        String logMessage = "[" + severity + "] " + errorLocation + e.getMessage() + "\n";

        try {
            outputErrors.write(logMessage.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException("LBOMbl5O2s :: ", ex);
        }
    }

}
