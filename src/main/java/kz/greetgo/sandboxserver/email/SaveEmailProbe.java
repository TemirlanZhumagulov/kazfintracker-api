package kz.greetgo.sandboxserver.email;


import kz.greetgo.email.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class SaveEmailProbe {
    public static void main(String[] args) {

        Email email = new Email();
        email.setFrom("from@gmail.com");
        email.setTo("to@gmail.com");
        email.setSubject("Tem");
        email.setBody("Hello World!");
        {
            Attachment a = new Attachment();
            a.name = "name";
            a.data = "basdalsdjald и на русском можно пистаь ".getBytes(StandardCharsets.UTF_8);
            email.getAttachments().add(a);
        }
        // String
        EmailSerializerXml emailSerializer = new EmailSerializerXml();
        String str = emailSerializer.serialize(email);
        System.out.println(str);
        System.out.println(emailSerializer.deserialize(str));

        // File Variant
        File f = new File("build/email/to_send/MySendLetter.xml");
        emailSerializer.serialize(f, email);
        System.out.println(emailSerializer.deserialize(f));

        // OutputStream InputStream Variant
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        emailSerializer.serialize(outStream, email);
        System.out.println(outStream.toString(StandardCharsets.UTF_8));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
        System.out.println(emailSerializer.deserialize(inputStream));

//        EmailSaver emailSaver = new EmailSaver("MySendLetter", "build/email/to_send");
//
//        EmailSender emailSender = emailSaver;
//

//        emailSaver.send(email);
    }
}
