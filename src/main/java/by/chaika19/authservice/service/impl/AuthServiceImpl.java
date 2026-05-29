package by.chaika19.authservice.service.impl;

import by.chaika19.authservice.dto.request.LoginRequestDto;
import by.chaika19.authservice.dto.request.RegisterRequestDto;
import by.chaika19.authservice.dto.response.TokenResponseDto;
import by.chaika19.authservice.dto.response.TokenValidateResponseDto;
import by.chaika19.authservice.exception.BusinessException;
import by.chaika19.authservice.model.UserCredentials;
import by.chaika19.authservice.model.UserRole;
import by.chaika19.authservice.repository.UserCredentialsRepository;
import by.chaika19.authservice.security.JwtTokenProvider;
import by.chaika19.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(RegisterRequestDto regDto) {
        log.info("Attempting to register credentials for email: {}", regDto.login());

        if(credentialsRepository.existsByLogin(regDto.login())) {
            log.warn("Registration failed: email {} is already taken", regDto.login());
            throw new BusinessException("User with email " + regDto.login() + " already exists");
        }

        UserCredentials userCredentials = UserCredentials.builder()
                .login(regDto.login())
                .password(passwordEncoder.encode(regDto.password()))
                .role(UserRole.ROLE_USER)
                .name(regDto.name())
                .surname(regDto.surname())
                .birthDate(regDto.birthDate())
                .build();

        credentialsRepository.save(userCredentials);
        log.info("Credentials successfully registered for user with email: {}", regDto.login());
    }

    public TokenResponseDto login(LoginRequestDto logDto) {
        log.info("Login attempt for email: {}", logDto.login());

        UserCredentials userCredentials = credentialsRepository.findByLogin(logDto.login())
                .orElseThrow(() -> {
                    log.warn("Login failed: user with email {} not found", logDto.login());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(logDto.password(), userCredentials.getPassword())) {
            log.warn("Login failed: incorrect password for email {}", logDto.login());
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                userCredentials.getId(), userCredentials.getRole().name()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                userCredentials.getId(),
                userCredentials.getRole().name()
        );

        log.info("User {} successfully logged in. JWT tokens generated.", logDto.login());
        return new TokenResponseDto(accessToken, refreshToken);
    }

    public TokenValidateResponseDto validate(String token) {
        return jwtTokenProvider.parseAndValidateToken(token)
                .map(claims -> new TokenValidateResponseDto(
                        Long.parseLong(claims.getSubject()),
                        claims.get(JwtTokenProvider.CLAIM_ROLE, String.class)
                ))
                .orElseThrow(() -> {
                    log.warn("Token validation failed (expired or invalid signature)");
                    return new BadCredentialsException("Token is invalid or expired");
                });
    }

    public TokenResponseDto refresh(String refreshToken) {
        log.info("Token refresh request received");

        return jwtTokenProvider.parseAndValidateToken(refreshToken)
                .filter(claims -> JwtTokenProvider.TYPE_REFRESH.equals(claims.get(JwtTokenProvider.CLAIM_TYPE, String.class)))
                .map(claims -> {
                    Long id = Long.valueOf(claims.getSubject());

                    UserCredentials userCredentials = credentialsRepository.findById(id)
                            .orElseThrow(() -> {
                                log.warn("Refresh failed: credentials for user ID {} not found", id);
                                return new BusinessException("User session not found");
                            });

                    String currentRole = userCredentials.getRole().name();

                    String newAccessToken = jwtTokenProvider.generateAccessToken(id, currentRole);
                    String newRefreshToken = jwtTokenProvider.generateRefreshToken(id, currentRole);

                    log.info("JWT token pair successfully refreshed for user ID: {}", id);
                    return new TokenResponseDto(newAccessToken, newRefreshToken);
                })
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: invalid or expired refresh token");
                    return new BusinessException("Invalid or expired refresh token");
                });
    }
}
