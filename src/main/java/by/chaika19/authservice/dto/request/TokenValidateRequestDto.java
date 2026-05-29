package by.chaika19.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenValidateRequestDto(
        @NotBlank(message = "Token cannot be empty")
        String validateToken
) {}