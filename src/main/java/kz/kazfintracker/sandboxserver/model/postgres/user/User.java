package kz.kazfintracker.sandboxserver.model.postgres.user;

import kz.kazfintracker.sandboxserver.model.postgres.token.Token;
import kz.kazfintracker.sandboxserver.model.web.auth.UserDTO;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "_user")
public class User implements UserDetails {

  @Id
  @GeneratedValue
  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private String password;
  private ObjectId mongoId;
  @Lob
  private byte[] avatar;
  // add phones
  // add addresses

  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToMany(mappedBy = "user")
  private List<Token> tokens;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public UserDTO convertToDTO() {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(this.getId());
    userDTO.setFirstname(this.getFirstname());
    userDTO.setLastname(this.getLastname());
    userDTO.setEmail(this.getEmail());
    userDTO.setAvatar(this.getAvatar());
    return userDTO;
  }
}
