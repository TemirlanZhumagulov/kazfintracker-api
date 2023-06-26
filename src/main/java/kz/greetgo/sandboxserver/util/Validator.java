package kz.greetgo.sandboxserver.util;

import kz.greetgo.sandboxserver.model.web.upsert.ClientAccount;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;

public class Validator {

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
  public static void validate(ClientToUpsert client, boolean isCreate) {
    if (client == null) {
      throw new IllegalArgumentException("client cannot be null");
    }

    if (!isCreate && StrUtils.isNullOrBlank(client.getId())) {
      throw new IllegalArgumentException("id cannot be null");
    }

    if (StrUtils.isNullOrBlank(client.getName())) {
      throw new IllegalArgumentException("name cannot be null");
    }
    if (StrUtils.isNullOrBlank(client.getSurname())) {
      throw new IllegalArgumentException("surname cannot be null");
    }
    if (StrUtils.isNullOrBlank(client.getPatronymic())) {
      throw new IllegalArgumentException("patronymic cannot be null");
    }
    if(client.getCharm() == null) {
      throw new IllegalArgumentException("charm cannot be null");
    }
    if(client.getAccount() == null) {
      throw new IllegalArgumentException("account cannot be null");
    }
    if(client.getBirth_date() == null) {
      throw new IllegalArgumentException("birth_date cannot be null");
    }
    if(client.getAddresses() == null || client.getAddresses().isEmpty()){
      throw new IllegalArgumentException("addresses cannot be null or empty");
    }
    if(client.getPhones() == null || client.getPhones().isEmpty()){
      throw new IllegalArgumentException("phones cannot be null or empty");
    }
    // business logic (birth_date, charm, addresses, phones etc)
    ClientAccount account = client.getAccount();
    if (account.getMoney() < 0 || account.getMoney() > 1_000_000_000) {
      throw new IllegalArgumentException("Account Money is expected to be in range from 0 to 1 000 000 000");
    }
    if (account.getMinimumBalance() < 0 || account.getMinimumBalance() > 1_000_000_000) {
      throw new IllegalArgumentException("Account Minimum Balance is expected to be in range from 0 to 1 000 000 000");
    }
    if (account.getMaximumBalance() < 0 || account.getMaximumBalance() > 1_000_000_000) {
      throw new IllegalArgumentException("Account Maximum Balance is expected to be in range from 0 to 1 000 000 000");
    }
  }

}
