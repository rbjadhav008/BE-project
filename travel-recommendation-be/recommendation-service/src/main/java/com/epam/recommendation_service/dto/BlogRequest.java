package com.epam.recommendation_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BlogRequest {
    @NotBlank(message = "Title is required")
    @NotNull
    @Size(max = 255, message = "Title cannot be longer than 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @NotNull
    private String description;

    @NotBlank(message = "Category is required")
    @NotNull
    private String category;

    @NotBlank(message = "City is required")
    @NotNull
    private String city;

    @NotBlank(message = "Country is required")
    @NotNull
    private String country;

    private MultipartFile image;
}
