package kz.kazfintracker.sandboxserver.config;

import kz.greetgo.conf.hot.DefaultBoolValue;
import kz.greetgo.conf.hot.Description;

@Description("Params for consumers to ignore exceptions")
public interface IgnoreKafkaExceptionConfig {

  @Description("bank_account")
  @DefaultBoolValue(false)
  boolean bankAccount();

  @Description("client")
  @DefaultBoolValue(false)
  boolean client();

  @Description("model_a")
  @DefaultBoolValue(false)
  boolean modelA();

  @Description("transaction")
  @DefaultBoolValue(false)
  boolean transaction();

  @Description("transaction_category")
  @DefaultBoolValue(false)
  boolean transactionCategory();

  @Description("recur_transaction_amount")
  @DefaultBoolValue(false)
  boolean recurTransactionAmount();

  @Description("budget")
  @DefaultBoolValue(false)
  boolean budget();

  @Description("currency")
  @DefaultBoolValue(false)
  boolean currency();

}
