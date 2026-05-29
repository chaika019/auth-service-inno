package by.chaika19.authservice.security.seeder;

import by.chaika19.authservice.model.UserCredentials;
import by.chaika19.authservice.model.UserRole;
import by.chaika19.authservice.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_LOGIN}")
    private String adminLogin;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking if system administrator seeding is required...");

        if (!credentialsRepository.existsByRole(UserRole.ROLE_ADMIN)) {
            log.info("No administrator account found. Initiating dynamic seeder flow using credentials for: {}", adminLogin);

            UserCredentials admin = UserCredentials.builder()
                    .login(adminLogin)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ROLE_ADMIN)
                    .name("Admin")
                    .surname("Super")
                    .birthDate(LocalDate.of(2000, 6, 7))
                    .build();

            credentialsRepository.save(admin);
            log.info("System administrator account successfully initialized.");
        } else {
            log.info("System administrator initialization skipped: Admin account already exists.");
        }
    }
}