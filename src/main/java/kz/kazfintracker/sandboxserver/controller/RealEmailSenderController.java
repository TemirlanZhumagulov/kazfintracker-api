package kz.kazfintracker.sandboxserver.controller;


import kz.greetgo.email.Email;
import kz.kazfintracker.sandboxserver.register.RealEmailSenderRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a/tickets")
public class RealEmailSenderController {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private RealEmailSenderRegister emailSenderRegister;

    @PostMapping("/sendEmail")
    public void sendEmail(@RequestBody Email email) {
        emailSenderRegister.sendEmail(email);
    }

}
