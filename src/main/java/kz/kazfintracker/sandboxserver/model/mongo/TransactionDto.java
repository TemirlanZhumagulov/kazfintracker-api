package kz.kazfintracker.sandboxserver.model.mongo;

import kz.kazfintracker.sandboxserver.model.web.Transaction;
import kz.kazfintracker.sandboxserver.util.Ids;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
public class TransactionDto {
  public ObjectId id;
  public String date; // Consider using Date or LocalDate instead of String for date types
  public double amount;
  public int type;
  public String note;
  public int idCategory;
  public int idBankAccount;
  public int idBankAccountTransfer;
  public int recurring; // This is a boolean represented as an integer, consider using boolean
  public String recurrencyType;
  public int recurrencyPayDay;
  public String recurrencyFrom;
  public String recurrencyTo;
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime

  public Transaction toRead() {
    Transaction transaction = new Transaction();
    transaction.id = Ids.objectIdToInt(id);
    transaction.amount = amount;
    transaction.date = date;
    transaction.note = note;
    transaction.idCategory = idCategory;
    transaction.idBankAccount = idBankAccount;
    transaction.idBankAccountTransfer = idBankAccountTransfer;
    transaction.recurring = recurring;
    transaction.recurrencyType = recurrencyType;
    transaction.recurrencyPayDay = recurrencyPayDay;
    transaction.recurrencyFrom = recurrencyFrom;
    transaction.recurrencyTo = recurrencyTo;
    transaction.createdAt = createdAt;
    transaction.updatedAt = updatedAt;
    return transaction;
  }

}
