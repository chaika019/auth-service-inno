package by.chaika19.authservice.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequestDto(

        //email типо
        @NotBlank(message = "Login (email) should not be blank")
        @Size(min = 3, max = 50, message = "Login should be between {min} and {max} characters")
        @Email(message = "Invalid email")
        String login,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 30, message = "Password should be between {min} and {max} characters")
        String password,

        @NotBlank(message = "Name should not be blank")
        @Size(min = 1, max = 255, message = "Name should be between {min} and {max} characters")
        String name,

        @NotBlank(message = "Surname should not be blank")
        @Size(min = 1, max = 255, message = "Surname should be between {min} and {max} characters")
        String surname,

        @NotNull(message = "Birth date should not be null")
        @Past(message = "Birth date should be in the past")
        LocalDate birthDate
) {
        @Override
        public String toString() {
                return "RegisterRequestDto{" +
                        "login='" + login + '\'' +
                        ", name='" + name + '\'' +
                        ", surname='" + surname + '\'' +
                        ", birthDate=" + birthDate +
                        '}';
        }
}
