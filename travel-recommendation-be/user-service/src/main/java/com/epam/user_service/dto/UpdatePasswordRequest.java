package com.epam.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UpdatePasswordRequest {
    @NotBlank(message = "invalid email")
    @Email(message = "this email is not valid")
    String email;
    @NotEmpty(message = "password cannot be empty")
    String password;
    @NotEmpty(message = "otp cannot empty")
    String otp;
}
