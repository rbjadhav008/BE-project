package com.epam.recommendation_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlogSummaryResponse {
    private int id;
    private String title;
    private String imageURL;
    private String description;
    private String city;
    private String country;
    private String category;
    private String author;
    private String authorImageURL;
    private String status;
    private LocalDateTime creationTime;
    private int isFavorite = 0;
}
