package com.epam.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String city;
    private String country;
    private String imageURL;
}
