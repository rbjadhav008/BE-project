package com.epam.recommendation_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogResponse {
    private int id;
    private String title;
    private String description;
    private String category;
    private String city;
    private String country;
    private String imageURL;
}
