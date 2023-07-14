package kz.greetgo.sandboxserver.email;

import org.springframework.mail.MailException;

public class ErrorFilters {

    public static class MailExceptionFilters {

        private final MailException error;

        private MailExceptionFilters(MailException error) {
            this.error = error;
        }

        private boolean ignoreUserUnknown = false;

        public MailExceptionFilters ignoreUserUnknown() {
            ignoreUserUnknown = true;
            return this;
        }

        public void check() {
            if (ignoreUserUnknown) {
                String message = error.getMessage();

                if (message != null && message.contains("User unknown")) {
                    return;
                }
            }
            throw error;
        }

    }


    public static MailExceptionFilters mail(MailException e) {
        return new MailExceptionFilters(e);
    }

}