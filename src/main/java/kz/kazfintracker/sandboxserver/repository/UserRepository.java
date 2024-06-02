package kz.kazfintracker.sandboxserver.repository;

import kz.kazfintracker.sandboxserver.model.postgres.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

}
