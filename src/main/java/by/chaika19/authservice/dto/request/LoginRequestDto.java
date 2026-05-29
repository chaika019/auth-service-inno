package by.chaika19.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Login (email) should not be blank")
        @Email(message = "Invalid email format")
        String login,

        @NotBlank(message = "Password is required")
        String password
) {
        @Override
        public String toString() {
                return "LoginRequestDto{" +
                        "email='" + login + '\'' +
                        '}';
        }
}
