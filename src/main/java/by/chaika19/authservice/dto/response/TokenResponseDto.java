package by.chaika19.authservice.dto.response;

public record TokenResponseDto(
        String accessToken,
        String refreshToken
) { }
