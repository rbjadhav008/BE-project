package com.epam.recommendation_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class Role {

    private Long id;
    private ERole name;
}

