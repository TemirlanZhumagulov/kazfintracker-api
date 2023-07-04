package kz.greetgo.sandboxserver.email;

import kz.greetgo.email.Attachment;
import kz.greetgo.email.Email;
import kz.greetgo.email.RealEmailSender;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractRealEmailSender implements RealEmailSender {
    protected abstract SendEmailConfig conf();

    @Override
    public void realSend(Email email) {

        final List<String> addressesToSend = Stream.concat(Stream.of(email.getTo()), email.getCopies().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (addressesToSend.isEmpty()) {
            return;
        }

        if (conf().useFake()) {
            trace(() -> "t4C8neHqM0 :: FAKE: Отправка мыла с параметрами: " +
                    "smtpHost = " + conf().smtpHost() + ", smtpPort = " + conf().smtpPort()
                    + ", username = " + conf().username() + ", addressesToSend = " + addressesToSend);
            return;
        }

        JavaMailSenderImpl mailSender = createJavaMailSenderImpl();



        String fromAddress = takeFromAddress(email);

        MimeMessage mimeMessage = convert(mailSender, email, addressesToSend, fromAddress);

        trace(() -> "Ys77B6eHZk :: Отправка мыла с параметрами: " +
                "smtpHost = " + conf().smtpHost() + ", smtpPort = " + conf().smtpPort() + ", username = " + conf().username()
                + ", fromAddress = " + fromAddress);

        try {
            mailSender.send(mimeMessage);
        } catch (MailException e) {
            ErrorFilters.mail(e).ignoreUserUnknown().check();
        }

    }

    protected JavaMailSenderImpl createJavaMailSenderImpl() {
        JavaMailSenderImpl impl = new JavaMailSenderImpl();
        impl.setHost(conf().smtpHost());
        impl.setPort(conf().smtpPort());
        impl.setUsername(conf().username());
        impl.setPassword(conf().password());

        Properties props = impl.getJavaMailProperties();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        populateJavaMailProperties(props);

        return impl;
    }

    protected void populateJavaMailProperties(Properties props) {}

    protected void trace(Supplier<String> message) {}

    private String takeFromAddress(Email email) {
        String fromAddress = email.getFrom();
        if (fromAddress == null || "null".equals(fromAddress)) {
            fromAddress = conf().sendFrom();
        }
        if (fromAddress == null || "null".equals(fromAddress)) {
            fromAddress = "root";
        }

        if (!fromAddress.contains("@")) {
            fromAddress += "@" + defaultEmailDomain();
        }
        return fromAddress;
    }

    protected String defaultEmailDomain() {
        return "greetgo.kz";
    }

    protected MimeMessage convert(JavaMailSenderImpl mailSender, Email email,
                                  List<String> toList, String fromAddress) {

        final MimeMessage ret = mailSender.createMimeMessage();

        try {

            MimeMessageHelper helper = new MimeMessageHelper(ret, true, "UTF-8");

            helper.setTo(toList.toArray(new String[0]));
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), true);

            if (email.getName() != null) {
                helper.setFrom(new InternetAddress(fromAddress, email.getName()));
            } else {
                helper.setFrom(fromAddress);
            }

            final List<Attachment> attachments = email.getAttachments();


            for (Attachment attachment : attachments) {
                final ByteArrayDataSource data = new ByteArrayDataSource(attachment.data, "text/html");
                helper.addAttachment(attachment.name, data);
            }

            return ret;

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
