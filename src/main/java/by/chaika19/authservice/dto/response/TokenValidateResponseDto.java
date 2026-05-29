package by.chaika19.authservice.dto.response;

public record TokenValidateResponseDto (
        Long userId,
        String role
) { }
