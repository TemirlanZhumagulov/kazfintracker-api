package kz.kazfintracker.sandboxserver.migration.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
@Slf4j
public class UnzipUtils {
    public static void uncompressFiles(String inputDirectoryName, String outputDirectoryName) {
        File directory = new File(inputDirectoryName);
        File[] files = directory.listFiles((dir, name) -> name.endsWith("xml.tar.bz2"));

        if (files == null) {
            log.error("xOHV93z9jj :: No XML files found in the directory.");
            return;
        }

        try {
            File outputDir = new File(outputDirectoryName);
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
                    log.info("Uncompressing file: " + outputFile.getName());
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

            log.info("Files uncompressed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
