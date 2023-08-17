package kz.greetgo.sandboxserver.util;

import kz.greetgo.sandboxserver.model.web.upsert.ClientAccount;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;

import java.time.LocalDate;

public class Validator {
    public static String validate(ClientToUpsert client, boolean isCreate) {
        if (client == null) {
            return "Error: Client cannot be empty";
        }
        if (!isCreate && StrUtils.isNullOrBlank(client.getId())) {
            return "Error: id cannot be empty";
        }

        if (StrUtils.isNullOrBlank(client.getName())) {
            return "Error: Name cannot be empty";
        }
        if (StrUtils.isNullOrBlank(client.getSurname())) {
            return "Error: Surname cannot be empty";
        }
        if (StrUtils.isNullOrBlank(client.getPatronymic())) {
            return "Error: Patronymic cannot be empty";
        }
        if (client.getCharm() == null) {
            return "Error: Charm cannot be empty";
        }
        if (client.getAccount() == null) {
            return "Error: Account data (total, min and max balance) cannot be empty";
        }
        if (client.getBirth_date() == null) {
            return "Error: Birth Date cannot be empty";
        }
        if (client.getAddresses() == null || client.getAddresses().isEmpty()) {
            return "Error: Addresses cannot be empty";
        }
        if (client.getHomePhone() == null) {
            return "Error: Home Phone cannot be empty";
        }
        if (client.getWorkPhone() == null) {
            return "Error: Work Phone cannot be empty";
        }
        if (client.getMobilePhone() == null) {
            return "Error: Mobile Phone cannot be empty";
        }
        // business logic (birth_date, charm, addresses, phones etc)
        ClientAccount account = client.getAccount();
        if (account.getTotal_balance() < 0 || account.getTotal_balance() > 1_000_000_000) {
            return "Error: Account Money is expected to be in range from 0 to 1_000_000_000";
        }
        if (account.getMin_balance() < 0 || account.getMin_balance() > 1_000_000_000) {
            return "Error: Account Minimum Balance is expected to be in range from 0 to 1_000_000_000";
        }
        if (account.getMax_balance() < 0 || account.getMax_balance() > 1_000_000_000) {
            return "Error: Account Maximum Balance is expected to be in range from 0 to 1_000_000_000";
        }
        if (client.getBirth_date().isAfter(LocalDate.now()) || client.getBirth_date().isBefore(LocalDate.of(1900, 1, 1))) {
            return "Error: Birth Date cannot be after now or before 1900 year:" + client.getBirth_date();
        }
        return null;
    }

    public static void validateA(TestModelAToUpsert testModel, boolean isCreate) {
        if (testModel == null) {
            throw new IllegalArgumentException("testModel cannot be null");
        }

        if (!isCreate && StrUtils.isNullOrBlank(testModel.id)) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (StrUtils.isNullOrBlank(testModel.strField)) {
            throw new IllegalArgumentException("strField cannot be null");
        }

        if (testModel.boolField == null) {
            throw new IllegalArgumentException("boolField cannot be null");
        }

        if (testModel.intField == null) {
            throw new IllegalArgumentException("intField cannot be null");
        }

        // business logic
        if (testModel.intField < 0 || testModel.intField > 1_000_000) {
            throw new IllegalArgumentException("intField is expected to be in range from 0 to 1 000 000");
        }

    }


}
