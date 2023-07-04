package kz.greetgo.sandboxserver.email;

import kz.greetgo.conf.hot.DefaultBoolValue;
import kz.greetgo.conf.hot.DefaultIntValue;
import kz.greetgo.conf.hot.DefaultStrValue;
import kz.greetgo.conf.hot.Description;

@Description("Параметры доступа к ящику, чтобы отправлять письма")
public interface SendEmailConfig {

    @Description("Use Fake")
    @DefaultBoolValue(false)
    boolean useFake();

    @Description("SMTP хост")
    @DefaultStrValue("smtp.yandex.kz")
    String smtpHost();

    @Description("SMTP порт")
    @DefaultIntValue(465)
    int smtpPort();

    @Description("Имя пользователя")
    @DefaultStrValue("noreply@mybpm.kz")
    String username();

    @Description("Пароль пользователя")
    @DefaultStrValue("***")
    String password();

    @Description("Значение поля from при отправке писем")
    @DefaultStrValue("noreply@mybpm.kz")
    String sendFrom();

    @Description("Значение свойства mail.smtp.auth")
    @DefaultBoolValue(true)
    boolean smtpAuth();

    @Description("Значение свойства mail.smtp.ssl.enable")
    @DefaultBoolValue(true)
    boolean smtpSslEnable();

    @Description("Значение свойства mail.smtp.starttls.enable")
    @DefaultBoolValue(true)
    boolean smtpStartTlsEnable();

    @Description("Значение свойства mail.mime.charset")
    @DefaultBoolValue(true)
    boolean mimeCharset();

}