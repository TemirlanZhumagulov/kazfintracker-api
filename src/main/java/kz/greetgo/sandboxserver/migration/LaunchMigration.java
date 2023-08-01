package kz.greetgo.sandboxserver.migration;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

import static kz.greetgo.sandboxserver.migration.DatabaseSetup.createActualTables;
import static kz.greetgo.sandboxserver.migration.DatabaseSetup.dropActualTables;
import static kz.greetgo.sandboxserver.migration.util.LogUtils.logErrorsToFile;
import static kz.greetgo.sandboxserver.migration.util.UnzipUtils.uncompressFiles;

@Slf4j
public class LaunchMigration {
    public static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        String zippedFilesDir = "build/out_files";
        String unzippedFilesDir = "build/xml_files/";

        uncompressFiles(zippedFilesDir, unzippedFilesDir);

        File[] xmlFiles = new File(unzippedFilesDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (xmlFiles == null) {
            log.error("rb2VHYtjxj :: No XML files found in the directory.");
            return;
        }

        try (CiaMigration migration = new CiaMigration(DatabaseSetup.getConnection())) {

            dropActualTables();
            createActualTables();

            for (File xmlFile : xmlFiles) {
                log.info("Reading file - " + xmlFile.getName());

                InputStream inputStream = transformXMLtoInputStream(xmlFile);
                OutputStream outputStream = new ByteArrayOutputStream();

                if (inputStream == null) {
                    log.error("9u6nSrS68w :: XML file " + xmlFile.getName() + " can't be transformed to InputStream");
                    continue;
                }

                migration.inputData = inputStream;
                migration.outputStream = outputStream;

                migration.migrate();

                String outputFile = "build/logs/" + migration.errorFileName;

                logErrorsToFile(outputStream, outputFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("FAo7yQlZS6 :: ", e);
        }

    }

    protected static ByteArrayInputStream transformXMLtoInputStream(File xmlFile) {
        try (FileInputStream fis = new FileInputStream(xmlFile);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] data = outputStream.toByteArray();

            return new ByteArrayInputStream(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}