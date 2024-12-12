package com.epam.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AdminUserDTO {

    @Email
    @NotEmpty
    @NotBlank
    private String email;

    @NotEmpty(message = "Invalid entry")
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Invalid entry")
    private String firstName;

    @NotEmpty(message = "Invalid entry")
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Invalid entry")
    private String lastName;

    @NotEmpty(message = "Invalid entry")
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Invalid entry")
    private String city;

    @NotEmpty(message = "Invalid entry")
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Invalid entry")
    private String country;

    private String password;

    private MultipartFile profileImage;

    private String status;

    private String role;

    private String imageURL;

}
