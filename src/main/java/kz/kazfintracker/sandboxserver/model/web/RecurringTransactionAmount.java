package kz.kazfintracker.sandboxserver.model.web;

import lombok.Getter;

@Getter
public class RecurringTransactionAmount {
  public Integer id;
  public String from; // Consider using Date or LocalDate
  public String to; // Consider using Date or LocalDate
  public double amount;
  public int idTransaction;
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}