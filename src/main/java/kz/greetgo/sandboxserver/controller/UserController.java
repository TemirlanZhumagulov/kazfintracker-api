package kz.greetgo.sandboxserver.controller;

import kz.greetgo.sandboxserver.model.postgres.user.ChangePasswordRequest;
import kz.greetgo.sandboxserver.impl.UserService;
import kz.greetgo.sandboxserver.model.postgres.user.User;
import kz.greetgo.sandboxserver.model.web.auth.UpdateUserRequest;
import kz.greetgo.sandboxserver.model.web.auth.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService service;

  @PatchMapping
  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                          Principal connectedUser) {
    service.changePassword(request, connectedUser);
    return ResponseEntity.ok().build();
  }


  @GetMapping("/current")
  public ResponseEntity<UserDTO> getUser(Principal connectedUser) {
    User user = service.getCurrentUser(connectedUser);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(user.convertToDTO());
  }

  @PutMapping("/current")
  public ResponseEntity<UserDTO> updateUser(@RequestBody UpdateUserRequest updateUserRequest,
                                             Principal connectedUser) {
    User updatedUser;
    try {
      updatedUser = service.updateUser(updateUserRequest, connectedUser);
      if (updatedUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    return ResponseEntity.ok(updatedUser.convertToDTO());
  }
}
