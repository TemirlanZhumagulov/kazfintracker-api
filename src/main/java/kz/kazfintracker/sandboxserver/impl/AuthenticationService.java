package kz.kazfintracker.sandboxserver.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kazfintracker.sandboxserver.auth.JwtService;
import kz.kazfintracker.sandboxserver.model.mongo.ClientDto;
import kz.kazfintracker.sandboxserver.model.web.auth.AuthenticationRequest;
import kz.kazfintracker.sandboxserver.model.web.auth.AuthenticationResponse;
import kz.kazfintracker.sandboxserver.model.web.auth.RegisterRequest;
import kz.kazfintracker.sandboxserver.mongo.MongoAccess;
import kz.kazfintracker.sandboxserver.model.postgres.token.Token;
import kz.kazfintracker.sandboxserver.repository.TokenRepository;
import kz.kazfintracker.sandboxserver.model.postgres.token.TokenType;
import kz.kazfintracker.sandboxserver.model.postgres.user.Role;
import kz.kazfintracker.sandboxserver.model.postgres.user.User;
import kz.kazfintracker.sandboxserver.repository.UserRepository;
import kz.kazfintracker.sandboxserver.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Each method here will return you two tokens: auth-token and refresh-token
 * The difference between them is an expiration date:
 * the first one exists for a day, and the second one for 7 days. Why?
 * 1) if the first token expires you don't need to authenticate and pass your login and password through the internet
 * Just use refresh-token, which always expires longer than access_token allowing you to get a new pair of tokens
 * 2) if criminal steals auth-token, you can refresh-token, and he won't be able to use this
 * if criminal steals refresh-token, you can authenticate, and he won't be able to use this
 * Because all tokens will be revoked
 * Finally, the same technique is used in oAuth2
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final MongoAccess mongoAccess;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {
    validateRegisterRequest(request);

    User user = User.builder()
      .firstname(request.getFirstname())
      .lastname(request.getLastname())
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .role(Role.USER)
      .mongoId(Ids.generate())
      .build();

    ClientDto clientDto = new ClientDto();
    clientDto.id = user.getMongoId();
    clientDto.name = user.getFirstname();
    clientDto.surname = user.getLastname();

    User savedUser = repository.save(user);
    mongoAccess.client().insertOne(clientDto);

    String jwtToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);
    return AuthenticationResponse.builder()
      .accessToken(jwtToken)
      .refreshToken(refreshToken)
      .build();
  }

  private void validateRegisterRequest(RegisterRequest request) {
    if (request.getFirstname() == null || request.getFirstname().isEmpty()) {
      throw new RuntimeException("Please fill out your firstname!");
    }
    if (request.getLastname() == null || request.getLastname().isEmpty()) {
      throw new RuntimeException("Please fill out your lastname!");
    }

    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.getEmail(),
        request.getPassword()
      )
    );
    var user = repository.findByEmail(request.getEmail())
      .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
      .accessToken(jwtToken)
      .refreshToken(refreshToken)
      .build();
  }

  private void validateEmail(String email) {
    if (email == null || email.isEmpty()) {
      throw new RuntimeException("You haven't filled out your email!");
    }

    if (!email.contains("@")) {
      throw new RuntimeException("Incorrect email!");
    }
    // todo optimize change find to count
    if (repository.findByEmail(email).isPresent()) {
      throw new RuntimeException("There is already an existing user with this email!");
    }
  }

  private void validatePassword(String password) {
    if (password == null || password.length() > 8) {
      throw new RuntimeException("You haven't filled out your password!");
    }
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
      .user(user)
      .token(jwtToken)
      .tokenType(TokenType.BEARER)
      .expired(false)
      .revoked(false)
      .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmail(userEmail)
        .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
