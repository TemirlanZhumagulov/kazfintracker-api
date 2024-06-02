package kz.kazfintracker.sandboxserver.model.web.auth;

import lombok.Data;


@Data
public class UserDTO {

  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private String password;
  private byte[] avatar;

}
