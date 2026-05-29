package by.chaika19.authservice.repository;

import by.chaika19.authservice.model.UserCredentials;
import by.chaika19.authservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {

    Optional<UserCredentials> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByRole(UserRole role);

}
