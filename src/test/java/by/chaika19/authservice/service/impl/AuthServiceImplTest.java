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
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserCredentialsRepository credentialsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserCredentials user;
    private final String login = "testuser@gmail.com";
    private final String rawPassword = "password123";
    private final String encodedPassword = "encoded_password_123";

    @BeforeEach
    void setUp() {
        user = UserCredentials.builder()
                .id(1L)
                .login(login)
                .password(encodedPassword)
                .role(UserRole.ROLE_USER)
                .name("Ivan")
                .surname("Ivanov")
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Nested
    class RegisterTests {

        @Test
        void register_Success() {
            RegisterRequestDto regDto = new RegisterRequestDto(login, rawPassword, "Ivan", "Ivanov", LocalDate.of(2000, 1, 1));

            when(credentialsRepository.existsByLogin(login)).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

            assertDoesNotThrow(() -> authService.register(regDto));

            verify(credentialsRepository, times(1)).save(any(UserCredentials.class));
        }

        @Test
        void register_ThrowsBusinessException_WhenUserAlreadyExists() {
            RegisterRequestDto regDto = new RegisterRequestDto(login, rawPassword, "Ivan", "Ivanov", LocalDate.of(2000, 1, 1));

            when(credentialsRepository.existsByLogin(login)).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(regDto));
            assertTrue(exception.getMessage().contains("already exists"));

            verify(credentialsRepository, never()).save(any());
        }
    }

    @Nested
    class LoginTests {

        @Test
        void login_Success() {
            LoginRequestDto logDto = new LoginRequestDto(login, rawPassword);
            String mockAccessToken = "mock-access-token";
            String mockRefreshToken = "mock-refresh-token";

            when(credentialsRepository.findByLogin(login)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name())).thenReturn(mockAccessToken);
            when(jwtTokenProvider.generateRefreshToken(user.getId(), user.getRole().name())).thenReturn(mockRefreshToken);

            TokenResponseDto response = authService.login(logDto);

            assertNotNull(response);
            assertEquals(mockAccessToken, response.accessToken());
            assertEquals(mockRefreshToken, response.refreshToken());
        }

        @Test
        void login_ThrowsBadCredentialsException_WhenUserNotFound() {
            LoginRequestDto logDto = new LoginRequestDto("wrong@gmail.com", rawPassword);

            when(credentialsRepository.findByLogin("wrong@gmail.com")).thenReturn(Optional.empty());

            assertThrows(BadCredentialsException.class, () -> authService.login(logDto));
            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        void login_ThrowsBadCredentialsException_WhenPasswordIncorrect() {
            LoginRequestDto logDto = new LoginRequestDto(login, "wrong_password");

            when(credentialsRepository.findByLogin(login)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong_password", encodedPassword)).thenReturn(false);

            assertThrows(BadCredentialsException.class, () -> authService.login(logDto));
            verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString());
        }
    }

    @Nested
    class ValidateTests {

        @Test
        void validate_Success() {
            String mockToken = "valid-token";
            Claims mockClaims = mock(Claims.class);

            when(mockClaims.getSubject()).thenReturn("1");
            when(mockClaims.get(JwtTokenProvider.CLAIM_ROLE, String.class)).thenReturn("ROLE_USER");
            when(jwtTokenProvider.parseAndValidateToken(mockToken)).thenReturn(Optional.of(mockClaims));

            TokenValidateResponseDto response = authService.validate(mockToken);

            assertNotNull(response);
            assertEquals(1L, response.userId());
            assertEquals("ROLE_USER", response.role());
        }

        @Test
        void validate_ThrowsBadCredentialsException_WhenTokenInvalidOrExpired() {
            String mockToken = "invalid-token";
            when(jwtTokenProvider.parseAndValidateToken(mockToken)).thenReturn(Optional.empty());

            assertThrows(BadCredentialsException.class, () -> authService.validate(mockToken));
        }
    }

    @Nested
    class RefreshTests {

        @Test
        void refresh_Success() {
            String mockRefreshToken = "valid-refresh-token";
            String newAccessToken = "new-access-token";
            String newRefreshToken = "new-refresh-token";
            Claims mockClaims = mock(Claims.class);

            when(mockClaims.getSubject()).thenReturn("1");
            when(mockClaims.get(JwtTokenProvider.CLAIM_TYPE, String.class)).thenReturn(JwtTokenProvider.TYPE_REFRESH);

            when(jwtTokenProvider.parseAndValidateToken(mockRefreshToken)).thenReturn(Optional.of(mockClaims));
            when(credentialsRepository.findById(1L)).thenReturn(Optional.of(user));

            when(jwtTokenProvider.generateAccessToken(1L, "ROLE_USER")).thenReturn(newAccessToken);
            when(jwtTokenProvider.generateRefreshToken(1L, "ROLE_USER")).thenReturn(newRefreshToken);

            TokenResponseDto response = authService.refresh(mockRefreshToken);

            assertNotNull(response);
            assertEquals(newAccessToken, response.accessToken());
            assertEquals(newRefreshToken, response.refreshToken());
        }

        @Test
        void refresh_ThrowsBusinessException_WhenTokenIsNotRefreshType() {
            String mockToken = "access-token-passed-instead";
            Claims mockClaims = mock(Claims.class);

            when(mockClaims.get(JwtTokenProvider.CLAIM_TYPE, String.class)).thenReturn(JwtTokenProvider.TYPE_ACCESS);
            when(jwtTokenProvider.parseAndValidateToken(mockToken)).thenReturn(Optional.of(mockClaims));

            assertThrows(BusinessException.class, () -> authService.refresh(mockToken));
            verify(credentialsRepository, never()).findById(anyLong());
        }

        @Test
        void refresh_ThrowsBusinessException_WhenUserNotFound() {
            String mockRefreshToken = "valid-refresh-token";
            Claims mockClaims = mock(Claims.class);

            when(mockClaims.getSubject()).thenReturn("1");
            when(mockClaims.get(JwtTokenProvider.CLAIM_TYPE, String.class)).thenReturn(JwtTokenProvider.TYPE_REFRESH);

            when(jwtTokenProvider.parseAndValidateToken(mockRefreshToken)).thenReturn(Optional.of(mockClaims));
            when(credentialsRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> authService.refresh(mockRefreshToken));
            verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString());
        }
    }
}