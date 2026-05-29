package by.chaika19.authservice.dto.response;

public record TokenValidateResponseDto (
        boolean valid,
        Long userId,
        String role
) { }
