package kz.kazfintracker.sandboxserver.model.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
  public Integer id;
  public String symbol;
  public String code;
  public String name;
  public int mainCurrency; // Consider using boolean
}
