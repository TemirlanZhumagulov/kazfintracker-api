package kz.greetgo.sandboxserver.migration.util;

import java.io.*;

import static kz.greetgo.sandboxserver.migration.LaunchMigration.BUFFER_SIZE;

public class LogUtils {
    public static void logErrorsToFile(OutputStream outputStream, String fileName) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
             ByteArrayInputStream inputStream = new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
