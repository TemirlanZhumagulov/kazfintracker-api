package kz.kazfintracker.sandboxserver.model.web;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {
  public Integer id;
  public String name;
  public String symbol;
  public int color;
  public double startingValue;
  public int active; // Consider using boolean
  public int mainAccount; // Consider using boolean
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}
