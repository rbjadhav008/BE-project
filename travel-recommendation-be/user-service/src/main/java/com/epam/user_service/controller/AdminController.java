package com.epam.user_service.controller;

import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.entity.Country;
import com.epam.user_service.entity.City;
import com.epam.user_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.entity.Category;
import com.epam.user_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.epam.user_service.dto.*;
import com.epam.user_service.service.AdminService;
import com.epam.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.List;
@RestController
@RequestMapping("api/admin")
@AllArgsConstructor
public class AdminController {
     @Autowired
    UserService userService;
    @Autowired
    AdminService adminService;


    @PostMapping("/addCountry")
    public ResponseEntity<MessageResponse> addCountry(@Valid @RequestBody FieldRequest fieldRequest) {
        String result = adminService.addCountry(fieldRequest);
        return new ResponseEntity<>(new MessageResponse(result), HttpStatus.CREATED);
    }

    @PutMapping("/editCountry/{id}")
    public ResponseEntity<MessageResponse> editCountry(@Valid @PathVariable("id") int id, @Valid @RequestBody FieldRequest fieldRequest) {
        String result = adminService.editCountry(id, fieldRequest);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @DeleteMapping("/removeCountry/{id}")
    public ResponseEntity<MessageResponse> removeCountry(@PathVariable("id") int id) {
        String result = adminService.removeCountry(id);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @GetMapping("/getAllCountries")
    public ResponseEntity<?> getAllCountries() {
        List<Country> countries = adminService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @PatchMapping("/toggleCountry/{id}")
    public ResponseEntity<MessageResponse> toggleCountry(@PathVariable("id") int id) {
        String result = adminService.toggleCountryStatus(id);
        return ResponseEntity.ok(new MessageResponse(result));
    }
    

    @PostMapping("/addCity")
    public ResponseEntity<MessageResponse> addCity(@Valid @RequestBody FieldRequest fieldRequest) {
        String result = adminService.addCity(fieldRequest);
        return new ResponseEntity<>(new MessageResponse(result), HttpStatus.CREATED);
    }

    @PutMapping("editCity/{id}")
    public ResponseEntity<MessageResponse> editCity(@Valid @PathVariable("id") int id, @Valid @RequestBody FieldRequest fieldRequest) {
        String result = adminService.editCity(id, fieldRequest);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @DeleteMapping("removeCity/{id}")
    public ResponseEntity<MessageResponse> removeCity(@PathVariable("id") int id) {
        String result = adminService.removeCity(id);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @GetMapping("getAllCities")
    public ResponseEntity<?> getAllCities() {
        List<City> cities = adminService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @PatchMapping("/toggleCity/{id}")
    public ResponseEntity<MessageResponse> toggleCity(@PathVariable("id") int id) {
        String result = adminService.toggleCityStatus(id);
        return ResponseEntity.ok(new MessageResponse(result));
    }


    @PostMapping("/addCategory")
    public ResponseEntity<MessageResponse> addCategory(@Valid @RequestBody FieldRequest fieldRequest) {
        String result = adminService.addCategory(fieldRequest);
        return new ResponseEntity<>(new MessageResponse(result), HttpStatus.CREATED);
    }

    @PutMapping("editCategory/{id}")
    public ResponseEntity<MessageResponse> editCategory(@Valid @PathVariable("id") int id, @Valid @RequestBody FieldRequest fieldRequest) {
        String result = adminService.editCategory(id, fieldRequest);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @DeleteMapping("removeCategory/{id}")
    public ResponseEntity<MessageResponse> removeCategory(@PathVariable("id") int id) {
        String result = adminService.removeCategory(id);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @GetMapping("getAllCategory")
    public ResponseEntity<?> getAllCategory() {
        List<Category> categories = adminService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PatchMapping("/toggleCategory/{id}")
    public ResponseEntity<MessageResponse> toggleCategory(@PathVariable("id") int id) {
        String result = adminService.toggleCategoryStatus(id);
        return ResponseEntity.ok(new MessageResponse(result));
    }


    @PostMapping("add")
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user in the system and returns the user data along with a success message. " +
                    "Pre-validations include checking the format of email, ensuring the password meets complexity requirements, " +
                    "and verifying that the username doesn't already exist in the system.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))),
                    @ApiResponse(responseCode = "409", description = "User already exists or validation failed", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Bad Request if validation fails", content = @Content)
            }
    )
    public ResponseEntity<UserRegistrationResponse> registerUser( @Valid @ModelAttribute UserRegistrationRequest userRegistrationRequest) throws IOException, IOException {
        return new ResponseEntity<>(userService.registerUser(userRegistrationRequest), HttpStatus.CREATED);
    }

    @GetMapping("view/{email}")
    public ResponseEntity<AdminUserDTO> viewUser(@PathVariable String email){

        return ResponseEntity.ok(adminService.viewUser(email));
    }

    @GetMapping("view/all")
    public Page<AdminUserDTO> viewAllUsers(@RequestParam(defaultValue = "1") int pageNo,
                                           @RequestParam(defaultValue = "10") int pageSize){
        return adminService.viewAllUsers(pageNo,pageSize);
    }

    @PutMapping("changeStatus/{email}")
    public ResponseEntity<MessageResponse> disableUser(@PathVariable String email){

        return ResponseEntity.ok(adminService.changeUserStatus(email));
    }

    @PutMapping("update/{email}")
    public ResponseEntity<MessageResponse> updateUser(@Valid @ModelAttribute AdminUserDTO adminUserDTO,@PathVariable String email) throws IOException {
        return ResponseEntity.ok(adminService.updateUser(adminUserDTO,email));
    }

    @PutMapping("addAdmin/{email}")
    public ResponseEntity<MessageResponse> addAdmin(@PathVariable String email){

        return ResponseEntity.ok(adminService.addAdminRole(email));
    }
}