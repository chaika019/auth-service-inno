package by.chaika19.authservice.service;

import by.chaika19.authservice.dto.request.LoginRequestDto;
import by.chaika19.authservice.dto.request.RegisterRequestDto;
import by.chaika19.authservice.dto.response.TokenResponseDto;
import by.chaika19.authservice.dto.response.TokenValidateResponseDto;

public interface AuthService {
    void register(RegisterRequestDto regDto);
    TokenResponseDto login(LoginRequestDto logDto);
    TokenValidateResponseDto validate(String token);
    TokenResponseDto refresh(String refreshToken);
}
