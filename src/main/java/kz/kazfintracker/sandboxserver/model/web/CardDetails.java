package kz.kazfintracker.sandboxserver.model.web;

import lombok.Data;

@Data
public class CardDetails {
  private String cardNumber;
  private String cardType;
  private String expirationDate;
  private String securityCode;
  private String billingAddress;
}
