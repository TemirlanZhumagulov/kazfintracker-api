package kz.kazfintracker.sandboxserver.model.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTransaction {
  public Integer id;
  public String name;
  public String symbol;
  public int color;
  public String note;
  public Integer parent;
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}
