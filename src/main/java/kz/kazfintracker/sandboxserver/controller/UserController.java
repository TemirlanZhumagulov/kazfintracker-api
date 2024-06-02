package kz.kazfintracker.sandboxserver.controller;

import kz.kazfintracker.sandboxserver.model.postgres.user.ChangePasswordRequest;
import kz.kazfintracker.sandboxserver.impl.UserService;
import kz.kazfintracker.sandboxserver.model.postgres.user.User;
import kz.kazfintracker.sandboxserver.model.web.auth.UpdateUserRequest;
import kz.kazfintracker.sandboxserver.model.web.auth.UserDTO;
import lombok.RequiredArgsConstructor;
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

  @PatchMapping("/current")
  public ResponseEntity<UserDTO> updateUser(@RequestBody UpdateUserRequest updateUserRequest,
                                            Principal connectedUser) throws IOException {
    User updatedUser = service.updateUser(updateUserRequest, connectedUser);
    return ResponseEntity.ok(updatedUser.convertToDTO());
  }

  @GetMapping("/current")
  public ResponseEntity<UserDTO> getUser(Principal connectedUser) {
    User user = service.getCurrentUser(connectedUser);
    return ResponseEntity.ok(user.convertToDTO());
  }

}
