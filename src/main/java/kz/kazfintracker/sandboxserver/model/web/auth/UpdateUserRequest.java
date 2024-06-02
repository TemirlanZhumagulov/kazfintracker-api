package kz.kazfintracker.sandboxserver.model.web.auth;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserRequest {
  private String firstname;
  private String lastname;
  private MultipartFile avatar;
}
