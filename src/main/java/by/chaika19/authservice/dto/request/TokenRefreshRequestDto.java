package by.chaika19.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequestDto(
        @NotBlank(message = "Refresh token cannot be empty")
        String refreshToken
) {}