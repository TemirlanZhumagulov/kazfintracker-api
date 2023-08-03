package kz.greetgo.sandboxserver.migration;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

import static kz.greetgo.sandboxserver.migration.DatabaseSetup.createSourceTables;
import static kz.greetgo.sandboxserver.migration.DatabaseSetup.dropSourceTables;
import static kz.greetgo.sandboxserver.migration.util.UnzipUtils.uncompressFiles;

@Slf4j
public class LaunchMigration {

    public static void main(String[] args) {
        String zippedFilesDir = "build/out_files";
        String unzippedFilesDir = "build/xml_files/";

        uncompressFiles(zippedFilesDir, unzippedFilesDir);

        File[] xmlFiles = new File(unzippedFilesDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (xmlFiles == null) {
            log.error("O00W8796WE :: No XML files found in the directory.");
            return;
        }

        try (CiaMigration migration = new CiaMigration(DatabaseSetup.getConnection())) {

            dropSourceTables();
            createSourceTables();

            for (File xmlFile : xmlFiles) {
                log.info("I7VpYZrfFd :: Reading file: " + xmlFile.getName());

                String outputFile = "build/logs/" + xmlFile.getName() + "_errors.log";

                try (InputStream inputStream = new FileInputStream(xmlFile);
                     OutputStream outputStream = new FileOutputStream(outputFile)) {

                    migration.inputData = inputStream;
                    migration.outputErrors = outputStream;

                    migration.migrate();

                }
            }
        } catch (Exception e) {
            throw new RuntimeException("FAo7yQlZS6 :: ", e);
        }

    }


}