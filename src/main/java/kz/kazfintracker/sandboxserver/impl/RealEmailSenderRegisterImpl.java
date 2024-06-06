package kz.kazfintracker.sandboxserver.impl;

import kz.greetgo.email.Email;
import kz.greetgo.email.RealEmailSender;
import kz.kazfintracker.sandboxserver.register.RealEmailSenderRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RealEmailSenderRegisterImpl implements RealEmailSenderRegister {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private RealEmailSender realEmailSender;

    @Override
    public void sendEmail(Email email) {
        realEmailSender.realSend(email);
    }


}