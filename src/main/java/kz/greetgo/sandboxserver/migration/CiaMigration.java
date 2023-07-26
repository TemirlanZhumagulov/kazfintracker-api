package kz.greetgo.sandboxserver.migration;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static kz.greetgo.sandboxserver.migration.util.TimeUtils.showTime;

public class CiaMigration implements Closeable {
    public static int downloadMaxBatchSize = 50_000;
    public static int uploadMaxBatchSize = 50_000;
    public static String directoryForUnzipping = GenerateInputFiles.DIR;
    public static String directoryForReading = "build/xml_files/";
    private static String tmpClientTable;
    private static String tmpPhoneTable;
    private static String logFileName;
    private static Connection operConnection = null;

    @Override
    public void close() {
        closeOperConnection();
    }

    private void closeOperConnection() {
        if (operConnection != null) {
            try {
                operConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            operConnection = null;
        }
    }

    private static void info(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        System.out.println(sdf.format(new Date()) + " [" + CiaMigration.class.getSimpleName() + "] " + message);
    }

    private static void exec(String sql) throws SQLException {
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

    public void migrate() {
        uncompressFiles();
        download();
        migrateFromTmp();
    }

    private void uncompressFiles() {
        File directory = new File(directoryForUnzipping);
        File[] files = directory.listFiles((dir, name) -> name.endsWith("xml.tar.bz2"));
        try {
            File outputDir = new File(directoryForReading);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            for (File file : files) {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                BZip2CompressorInputStream bz2InputStream = new BZip2CompressorInputStream(bufferedInputStream);
                TarArchiveInputStream tarInputStream = new TarArchiveInputStream(bz2InputStream);

                TarArchiveEntry entry;
                while ((entry = tarInputStream.getNextTarEntry()) != null) {
                    File outputFile = new File(outputDir, entry.getName().substring(15));
                    info("Uncompressing file... - " + outputFile.getName());
                    if (entry.isDirectory()) {
                        outputFile.mkdirs();
                    } else {
                        outputFile.getParentFile().mkdirs();
                        OutputStream outputFileStream = new FileOutputStream(outputFile);
                        IOUtils.copy(tarInputStream, outputFileStream);
                        outputFileStream.close();
                    }
                }

                tarInputStream.close();
                bz2InputStream.close();
                bufferedInputStream.close();
                fileInputStream.close();
            }

            info("Files uncompressed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void download() {
        File outputDirectory = new File(directoryForReading);
        File[] xmlFiles = outputDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (xmlFiles == null) {
            System.out.println("No XML files found in the directory.");
            return;
        }

        long startedAt = System.nanoTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date nowDate = new Date();
        tmpClientTable = "client_tmp_" + sdf.format(nowDate);
        tmpPhoneTable = "phone_tmp_" + sdf.format(nowDate);
        info("client_tmp = " + tmpClientTable);

        operConnection = DatabaseSetup.dropCreateTables(tmpClientTable, tmpPhoneTable);


        String insertClientSQL = "INSERT INTO " + tmpClientTable + " (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO " + tmpPhoneTable + " (client_id, type, number) VALUES (?,?,?)";
        try (PreparedStatement ciaPS = operConnection.prepareStatement(insertClientSQL);
             PreparedStatement phonesPS = operConnection.prepareStatement(insertPhonesPS)) {
            int recordsCount = 0;
            operConnection.setAutoCommit(false);
            for (File xmlFile : xmlFiles) {
                System.out.println("Reading file: " + xmlFile.getName());
                FileInputStream fileInputStream = new FileInputStream(xmlFile);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                MySAXHandler handler = new MySAXHandler(operConnection, ciaPS, phonesPS, startedAt, recordsCount);
                saxParser.parse(fileInputStream, handler);

                fileInputStream.close();
            }
            operConnection.setAutoCommit(true);
        } catch (IOException | ParserConfigurationException | SAXException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void migrateFromTmp() {
        //language=PostgreSQL
        try {
            //language=PostgreSQL
            exec("UPDATE " + tmpClientTable + " SET error = 'surname is not defined', status='ERROR' " +
                    "WHERE error = '' and (surname is null or surname = '')"); // TO DO ADD TRIM()
            //language=PostgreSQL
            exec("UPDATE " + tmpClientTable + " SET error = 'name is not defined', status='ERROR' " +
                    "WHERE error = '' and (name is null or name = '')"); // TO DO ADD TRIM()
            //language=PostgreSQL
            exec("UPDATE " + tmpClientTable + " SET error = 'birth_date is not defined', status='ERROR' " +
                    "WHERE error = '' and (birth is null or birth = '')"); // TO DO ADD TRIM()
            //language=PostgreSQL
            exec("UPDATE " + tmpClientTable + " SET error = 'birth_date is not correct', status='ERROR'" +
                    "WHERE error = '' and NOT birth ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}$';");
            //language=PostgreSQL
            exec("UPDATE " + tmpClientTable +
                    " SET error = 'AGE_OUT_OF_RANGE', status = 'ERROR'" +
                    " WHERE error = '' and EXTRACT(YEAR FROM AGE(NOW(), TO_DATE(birth, 'YYYY-MM-DD'))) NOT BETWEEN 18 AND 100");
            //language=PostgreSQL
            exec("UPDATE " + tmpClientTable + " AS c1 SET error = 'DUPLICATE', status = 'ERROR' " +
                    "FROM (" +
                    "    SELECT client_id, MAX(id) AS max_id " +
                    "    FROM " + tmpClientTable +
                    "    WHERE error = ''" +
                    "    GROUP BY client_id " +
                    "    HAVING COUNT(*) > 1 " +
                    ") AS c2 WHERE c1.client_id = c2.client_id AND c1.id < c2.max_id;"); // to do optimize takes 40 seconds

            operConnection.close();
            operConnection = DatabaseSetup.dropCreateActualTables();

            //language=PostgreSQL
            exec("INSERT INTO charm (name) SELECT DISTINCT charm FROM " + tmpClientTable);
//            //language=PostgreSQL
//            exec("CREATE INDEX ON charm (name)");
            //language=PostgreSQL
            exec("INSERT INTO client (id, surname, name, patronymic, gender, birth_date, charm_id) " +
                    "SELECT client_id, surname, name, patronymic, gender, TO_DATE(birth, 'YYYY-MM-DD'), (SELECT id FROM charm WHERE name = charm) FROM " + tmpClientTable + " WHERE status != 'ERROR' " +
                    "ON CONFLICT (id) DO UPDATE SET surname = excluded.surname, name = excluded.name, patronymic = excluded.patronymic, gender = excluded.gender, birth_date = excluded.birth_date, charm_id = excluded.charm_id;");
//            //language=PostgreSQL
//            exec("CREATE INDEX ON client (id)");
            //language=PostgreSQL
            exec("INSERT INTO client_phone (client, number, type) " +
                    "SELECT client_id, number, type FROM " + tmpPhoneTable + " WHERE client_id IN (SELECT id FROM client)");  // to do optimize takes 1 minute (CTE + JOIN OR THIS)
            //language=PostgreSQL
            exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                    "SELECT client_id, 'FACT', fact_street, fact_house, fact_flat FROM " + tmpClientTable + " WHERE status != 'ERROR' " +
                    "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
            //language=PostgreSQL
            exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                    "SELECT client_id, 'REG', register_street, register_house, register_flat FROM " + tmpClientTable + " WHERE status != 'ERROR' " +
                    "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
            uploadErrors();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void uploadErrors() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date nowDate = new Date();
        logFileName = "database_errors_" + sdf.format(nowDate) + ".csv";
        info("log file name = " + logFileName);


        String filePath = "build/logs/" + logFileName;
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (Statement statement = operConnection.createStatement();
             BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            int offset = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                String sql = "SELECT client_id, name, surname, birth, status, error FROM " + tmpClientTable + " WHERE status='ERROR' LIMIT " + uploadMaxBatchSize + " OFFSET " + offset;
                ResultSet resultSet = statement.executeQuery(sql);

                int rowCount = 0;

                while (resultSet.next()) {
                    String data = resultSet.getString("client_id") + "," + resultSet.getString("name") + "," + resultSet.getString("surname")
                            + "," + resultSet.getString("birth") + "," + resultSet.getString("status") + "," + resultSet.getString("error") + "\n";
                    writer.write(data);

                    rowCount++;
                }

                resultSet.close();

                hasMoreData = rowCount == uploadMaxBatchSize;

                offset += uploadMaxBatchSize;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}
