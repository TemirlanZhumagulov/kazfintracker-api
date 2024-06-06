package kz.kazfintracker.sandboxserver.config;


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
    @DefaultStrValue("smtp.gmail.com")
    String smtpHost();

    @Description("SMTP порт")
    @DefaultIntValue(587)
    int smtpPort();

    @Description("Имя пользователя")
    @DefaultStrValue("realnewyearpoop69@gmail.com") // Your Gmail
    String username();

    @Description("Пароль пользователя")
    @DefaultStrValue("beye bhxv hxcr clmh") // Your App Password
    String password();

    @Description("Значение поля from при отправке писем")
    @DefaultStrValue("realnewyearpoop69@gmail.com") // Your Gmail
    String sendFrom();

    @Description("Значение свойства mail.smtp.auth")
    @DefaultBoolValue(true)
    boolean smtpAuth();

    @Description("Значение свойства mail.smtp.ssl.enable")
    @DefaultBoolValue(true)
    boolean smtpSslEnable();


}
