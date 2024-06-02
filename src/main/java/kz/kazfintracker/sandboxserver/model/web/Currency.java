package kz.kazfintracker.sandboxserver.model.web;

import lombok.Getter;

@Getter
public class Currency {
  public Integer id;
  public String symbol;
  public String code;
  public String name;
  public int mainCurrency; // Consider using boolean
}
