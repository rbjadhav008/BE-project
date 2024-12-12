package com.epam.user_service.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.entity.Country;
import com.epam.user_service.entity.City;
import com.epam.user_service.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Arrays;

import com.epam.user_service.repository.CountryRepository;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.entity.City;
import com.epam.user_service.exception.FieldAlreadyExistsException;
import com.epam.user_service.exception.FieldNotFoundException;
import com.epam.user_service.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.entity.Category;
import com.epam.user_service.exception.FieldAlreadyExistsException;
import com.epam.user_service.exception.FieldNotFoundException;
import com.epam.user_service.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import com.epam.user_service.dto.AdminUserDTO;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.entity.ERole;
import com.epam.user_service.entity.Role;
import com.epam.user_service.entity.User;
import com.epam.user_service.exception.UserAlreadyExistsException;
import com.epam.user_service.exception.UserException;
import com.epam.user_service.exception.UserNotFound;
import com.epam.user_service.repository.RoleRepository;
import com.epam.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import com.epam.user_service.entity.Category;
import com.epam.user_service.dto.AdminUserDTO;
import com.epam.user_service.dto.UserRegistrationRequest;
import com.epam.user_service.dto.UserRegistrationResponse;
import com.epam.user_service.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import static org.mockito.MockitoAnnotations.openMocks;

public class AdminControllerTest {
    @Mock
    private CountryRepository countryRepository;


    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;
    @BeforeEach
    void setUp() {
        openMocks(this);
        userRegistrationRequest = new UserRegistrationRequest();
        adminUserDTO = new AdminUserDTO();
    }

   /* @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }*/

    @Test
    void testAddCountry() {
        FieldRequest dto = new FieldRequest("Germany");
        when(adminService.addCountry(dto)).thenReturn("Country added successfully");
        ResponseEntity<MessageResponse> response = adminController.addCountry(dto);
        assertEquals("Country added successfully", response.getBody().getMessage());
    }

    @Test
    void testEditCountry() {
        FieldRequest dto = new FieldRequest("France");
        int id = 5;
        when(adminService.editCountry(id, dto)).thenReturn("Country edited successfully");
        ResponseEntity<MessageResponse> response = adminController.editCountry(id, dto);
        assertEquals("Country edited successfully", response.getBody().getMessage());
    }

    @Test
    void testRemoveCountry() {
        int id = 5;
        when(adminService.removeCountry(id)).thenReturn("Country removed successfully");
        ResponseEntity<MessageResponse> response = adminController.removeCountry(id);
        assertEquals("Country removed successfully", response.getBody().getMessage());
    }

    @Test
    void testGetAllCountries() {
        List<Country> countries = Arrays.asList(new Country(), new Country());
        when(adminService.getAllCountries()).thenReturn(countries);
        ResponseEntity<?> response = adminController.getAllCountries();
        assertEquals(countries.size(), ((List<?>) response.getBody()).size());
    }

    @Test
    void shouldToggleCountryStatus() {
        int countryId = 1;
        String expectedMessage = "Country status toggled successfully.";
        when(adminService.toggleCountryStatus(countryId)).thenReturn(expectedMessage);

        ResponseEntity<MessageResponse> response = adminController.toggleCountry(countryId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMessage, response.getBody().getMessage());
        verify(adminService).toggleCountryStatus(countryId);
    }


    @Test
    void testAddCity() {
        FieldRequest dto = new FieldRequest("New York");
        when(adminService.addCity(dto)).thenReturn("City added successfully");
        ResponseEntity<MessageResponse> response = adminController.addCity(dto);
        assertEquals("City added successfully", response.getBody().getMessage());
    }

    @Test
    void testEditCity() {
        FieldRequest dto = new FieldRequest("Los Angeles");
        int id = 5;
        when(adminService.editCity(id, dto)).thenReturn("City edited successfully");
        ResponseEntity<MessageResponse> response = adminController.editCity(id, dto);
        assertEquals("City edited successfully", response.getBody().getMessage());
    }

    @Test
    void testRemoveCity() {
        int id = 5;
        when(adminService.removeCity(id)).thenReturn("City removed successfully");
        ResponseEntity<MessageResponse> response = adminController.removeCity(id);
        assertEquals("City removed successfully", response.getBody().getMessage());
    }

    @Test
    void testGetAllCities() {
        List<City> cities = Arrays.asList(new City(), new City());
        when(adminService.getAllCities()).thenReturn(cities);
        ResponseEntity<?> response = adminController.getAllCities();
        assertEquals(cities.size(), ((List<?>) response.getBody()).size());
    }

    @Test
    void shouldToggleCityStatus() {
        int cityId = 1;
        String expectedMessage = "City enabled successfully.";
        when(adminService.toggleCityStatus(cityId)).thenReturn(expectedMessage);

        ResponseEntity<MessageResponse> response = adminController.toggleCity(cityId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMessage, response.getBody().getMessage());
        verify(adminService).toggleCityStatus(cityId);
    }


  /*  @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }*/

    @Test
    void testAddCategory() {
        FieldRequest dto = new FieldRequest("Electronics");
        when(adminService.addCategory(dto)).thenReturn("Category added successfully");
        ResponseEntity<MessageResponse> response = adminController.addCategory(dto);
        assertEquals("Category added successfully", response.getBody().getMessage());
    }

    @Test
    void testEditCategory() {
        FieldRequest dto = new FieldRequest("Electronics");
        int id = 5;
        when(adminService.editCategory(id, dto)).thenReturn("Category edited successfully");
        ResponseEntity<MessageResponse> response = adminController.editCategory(id, dto);
        assertEquals("Category edited successfully", response.getBody().getMessage());
    }

    @Test
    void testRemoveCategory() {
        int id = 5;
        when(adminService.removeCategory(id)).thenReturn("Category removed successfully");
        ResponseEntity<MessageResponse> response = adminController.removeCategory(id);
        assertEquals("Category removed successfully", response.getBody().getMessage());
    }

    @Test
    void testGetAllCategories() {
        List<Category> categories = Arrays.asList(new Category(), new Category());
        when(adminService.getAllCategories()).thenReturn(categories);
        ResponseEntity<?> response = adminController.getAllCategory();
        assertEquals(categories.size(), ((List<?>) response.getBody()).size());
    }

    @Test
    void shouldToggleCategoryStatus() {
        int categoryId = 1;
        String expectedMessage = "Category enabled successfully.";
        when(adminService.toggleCategoryStatus(categoryId)).thenReturn(expectedMessage);

        ResponseEntity<MessageResponse> response = adminController.toggleCategory(categoryId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMessage, response.getBody().getMessage());
        verify(adminService).toggleCategoryStatus(categoryId);
    }
    @Mock
    private UserService userService;


    private UserRegistrationRequest userRegistrationRequest;
    private AdminUserDTO adminUserDTO;
    private String email = "test@example.com";



    @Test
    void registerUser_Success() throws IOException {
        UserRegistrationResponse response = new UserRegistrationResponse("User registered successfully", "test@example.com","","","","");
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        ResponseEntity<UserRegistrationResponse> result = adminController.registerUser(userRegistrationRequest);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void viewUser_Success() {
        when(adminService.viewUser(email)).thenReturn(adminUserDTO);

        AdminUserDTO result = adminController.viewUser(email).getBody();

        assertNotNull(result);
        verify(adminService).viewUser(email);
    }

    @Test
    void viewAllUsers_Success() {
        Page<AdminUserDTO> page = mock(Page.class);
        when(adminService.viewAllUsers(anyInt(), anyInt())).thenReturn(page);

        Page<AdminUserDTO> result = adminController.viewAllUsers(1, 10);

        assertNotNull(result);
        verify(adminService).viewAllUsers(1, 10);
    }

    @Test
    void disableUser_Success() {
        MessageResponse messageResponse = new MessageResponse("User status changed");
        when(adminService.changeUserStatus(email)).thenReturn(messageResponse);

        MessageResponse result = adminController.disableUser(email).getBody();

        assertNotNull(result);
        assertEquals("User status changed", result.getMessage());
        verify(adminService).changeUserStatus(email);
    }

    @Test
    void updateUser_Success() throws IOException {
        MessageResponse messageResponse = new MessageResponse("User updated successfully");
        when(adminService.updateUser(adminUserDTO, email)).thenReturn(messageResponse);

        MessageResponse result = adminController.updateUser(adminUserDTO, email).getBody();

        assertNotNull(result);
        assertEquals("User updated successfully", result.getMessage());
        verify(adminService).updateUser(adminUserDTO, email);
    }

    @Test
    void addAdmin_Success() {
        MessageResponse messageResponse = new MessageResponse("Admin role added successfully");
        when(adminService.addAdminRole(email)).thenReturn(messageResponse);

        MessageResponse result = adminController.addAdmin(email).getBody();

        assertNotNull(result);
        assertEquals("Admin role added successfully", result.getMessage());
        verify(adminService).addAdminRole(email);
    }

}