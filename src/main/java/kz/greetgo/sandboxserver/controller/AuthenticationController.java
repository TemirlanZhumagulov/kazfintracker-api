package kz.greetgo.sandboxserver.controller;

import kz.greetgo.sandboxserver.model.web.auth.AuthenticationRequest;
import kz.greetgo.sandboxserver.model.web.auth.AuthenticationResponse;
import kz.greetgo.sandboxserver.impl.AuthenticationService;
import kz.greetgo.sandboxserver.model.web.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
    try {
      return ResponseEntity.ok(service.register(request));
    } catch (Exception e) {
      AuthenticationResponse auth = new AuthenticationResponse();
      auth.setErrorMessage(e.getMessage());
      return ResponseEntity.badRequest().body(auth);
    }
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
    try {
      return ResponseEntity.ok(service.authenticate(request));
    } catch (Exception e) {
      AuthenticationResponse auth = new AuthenticationResponse();
      auth.setErrorMessage(e.getMessage());
      return ResponseEntity.badRequest().body(auth);
    }
  }

  @PostMapping("/refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    service.refreshToken(request, response);
  }


}
