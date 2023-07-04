package kz.greetgo.sandboxserver.email;


import kz.greetgo.email.Attachment;
import kz.greetgo.email.Email;
import kz.greetgo.email.RealEmailSender;

import java.nio.charset.StandardCharsets;

public class SendLetters {

    public static void main(String[] args) {
        Email email = new Email();
        email.setFrom("temirlanzhumagulov01@gmail.com");
        email.setTo("realnewyearpoop69@gmail.com");
        email.setSubject("Tem");
        email.setBody("Hello World!");
        {
            Attachment a = new Attachment();
            a.name = "name";
            a.data = "basdalsdjald и на русском можно пистаь ".getBytes(StandardCharsets.UTF_8);
            email.getAttachments().add(a);
        }

        RealEmailSender emailSender = new AbstractRealEmailSender() {
            @Override
            protected SendEmailConfig conf() {
                return new Config();
            }
        };

        emailSender.realSend(email);
    }
    static class Config implements SendEmailConfig{

        @Override
        public boolean useFake() {
            return false;
        }

        @Override
        public String smtpHost() {
            return "smtp.gmail.com";
        }

        @Override
        public int smtpPort() {
            return 465;
        }

        @Override
        public String username() {
            return "temirlanzhumagulov01@gmail.com";
        }

        @Override
        public String password() {
            return "QWERTY22186345GG";
        }

        @Override
        public String sendFrom() {
            return "temirlanzhumagulov01@gmail.com";
        }

        @Override
        public boolean smtpAuth() {
            return true;
        }

        @Override
        public boolean smtpSslEnable() {
            return true;
        }

        @Override
        public boolean smtpStartTlsEnable() {
            return true;
        }

        @Override
        public boolean mimeCharset() {
            return true;
        }
    }
}
