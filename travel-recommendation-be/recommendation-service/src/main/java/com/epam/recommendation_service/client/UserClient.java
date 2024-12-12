package com.epam.recommendation_service.client;

import com.epam.recommendation_service.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.epam.recommendation_service.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USER-SERVICE",url = "http://localhost:8082")
public interface UserClient {
    @GetMapping("/api/users/profile/{email}")
    UserProfileResponse getUserProfileByEmail(@PathVariable(value = "email",
            required = true) String email);

    @GetMapping("api/users/getUser")
    User getUserByEmail(@RequestParam("email") String email);
}
