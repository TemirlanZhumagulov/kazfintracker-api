package kz.kazfintracker.sandboxserver.model.web;

import lombok.Getter;

@Getter
public class Budget {
  public Integer id;
  public int idCategory;
  public String name;
  public double amountLimit;
  public int active; // Consider using boolean
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}
