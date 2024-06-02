package kz.kazfintracker.sandboxserver.controller;

import kz.kazfintracker.sandboxserver.model.web.CardDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class BankingApiController {

  @PostMapping("/link-card")
  public ResponseEntity<String> linkCard(@RequestBody @Valid CardDetails cardDetails) {
    // Basic validation logic (you can enhance this with custom validation as needed)
    if (!cardDetails.getCardNumber().matches("\\d{16}")) {  // Simple regex to check 16 digit number
      return ResponseEntity.badRequest().body("Invalid card number");
    }

    if (!cardDetails.getExpirationDate().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {  // MM/YY format
      return ResponseEntity.badRequest().body("Invalid expiration date");
    }

    if (!cardDetails.getSecurityCode().matches("\\d{3,4}")) {  // 3 or 4 digits
      return ResponseEntity.badRequest().body("Invalid security code");
    }

    // Assume validation passes
    return ResponseEntity.ok("Card linked successfully");
  }

}
