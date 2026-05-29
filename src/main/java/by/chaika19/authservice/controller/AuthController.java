package by.chaika19.authservice.controller;

import by.chaika19.authservice.dto.request.LoginRequestDto;
import by.chaika19.authservice.dto.request.RegisterRequestDto;
import by.chaika19.authservice.dto.request.TokenRefreshRequestDto;
import by.chaika19.authservice.dto.request.TokenValidateRequestDto;
import by.chaika19.authservice.dto.response.TokenResponseDto;
import by.chaika19.authservice.dto.response.TokenValidateResponseDto;
import by.chaika19.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto dto) {
        authService.register(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return new ResponseEntity<>(authService.login(dto), HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidateResponseDto> validate(@Valid @RequestBody TokenValidateRequestDto tokenValidateRequestDto) {
        TokenValidateResponseDto responseDto = authService.validate(tokenValidateRequestDto.validateToken());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@Valid @RequestBody TokenRefreshRequestDto tokenRefreshRequestDto) {
        TokenResponseDto responseDto = authService.refresh(tokenRefreshRequestDto.refreshToken());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
