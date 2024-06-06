package kz.kazfintracker.sandboxserver.util;

import kz.greetgo.email.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//
// This class is for example of using EmailSerializerXml and is not used in the code
// Attention! Here you are not sending any email, but just serializing and deserializing it
//
public class EmailSerializerXmlExample {
    public static void main(String[] args) throws IOException {

        Email email = new Email();
        email.setFrom("from@gmail.com");
        email.setTo("to@gmail.com");
        email.setSubject("Topic");
        email.getCopies().add("A2K1S2HD2K1L2AH32DK32LASH");
        email.setBody("Hello World!");
        {
            Attachment a = new Attachment();
            a.name = "Document 1";
            a.data = "Данные документа 1".getBytes(StandardCharsets.UTF_8);
            email.getAttachments().add(a);
        }
        {
            Attachment a = new Attachment();
            a.name = "Document 2";
            a.data = "Данные документа 2".getBytes(StandardCharsets.UTF_8);
            email.getAttachments().add(a);
        }

        EmailSerializerXml emailSerializer = new EmailSerializerXml();

        // String Variant
        System.out.println("******************* STRING VARIANT *******************");

        String str = emailSerializer.serialize(email);
        System.out.println(str);
        System.out.println(emailSerializer.deserialize(str));

        // File Variant
        System.out.println("******************* FILE VARIANT *******************");

        String filePath = "build/email/to_send/MySendLetter.xml";
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path.toAbsolutePath());
        }
        File file = path.toFile();

        emailSerializer.serialize(file, email);

        System.out.println(Files.readString(path, StandardCharsets.UTF_8));
        System.out.println(emailSerializer.deserialize(file));

        // OutputStream / InputStream Variant
        System.out.println("******************* I/O STREAM VARIANT *******************");

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        emailSerializer.serialize(outStream, email);
        System.out.println(outStream.toString(StandardCharsets.UTF_8));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
        System.out.println(emailSerializer.deserialize(inputStream));

    }
}

