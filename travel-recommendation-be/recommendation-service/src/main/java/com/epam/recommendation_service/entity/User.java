package com.epam.recommendation_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String password;

    private int isActive;

    private String city;

    private String country;

    private byte[] profileImage;

    private int isEnabled;

    private Set<Role> roles;

}
