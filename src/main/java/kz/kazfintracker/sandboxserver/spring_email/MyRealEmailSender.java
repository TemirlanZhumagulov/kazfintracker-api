package kz.kazfintracker.sandboxserver.spring_email;

import kz.kazfintracker.sandboxserver.config.SendEmailConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyRealEmailSender extends AbstractRealEmailSender {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private SendEmailConfig emailConfig;

    @Override
    protected SendEmailConfig conf() {
        return emailConfig;
    }

}
