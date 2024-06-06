package kz.kazfintracker.sandboxserver.impl;

import kz.greetgo.email.Attachment;
import kz.greetgo.email.Email;
import kz.kazfintracker.sandboxserver.ParentTestNG;
import kz.kazfintracker.sandboxserver.register.RealEmailSenderRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class RealEmailSenderRegisterImplTest extends ParentTestNG {

    @Autowired
    private RealEmailSenderRegister emailSenderRegister;

    @Test
    public void sendEmail___ShouldSendToGmail() {
        Email email = new Email();
        email.setTo("temirlanzhumagulov01@gmail.com"); // Set to
        email.setSubject("Topic");
        email.setBody("Hello World!");
        {
            Attachment a = new Attachment();
            a.name = "Название Документа";
            a.data = "Содержимое Документа...".getBytes(StandardCharsets.UTF_8);
            email.getAttachments().add(a);
        }

        //
        //
        emailSenderRegister.sendEmail(email);
        //
        //
    }


}
