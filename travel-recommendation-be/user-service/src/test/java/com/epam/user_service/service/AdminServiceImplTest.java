package com.epam.user_service.service;

import com.epam.user_service.dto.AdminUserDTO;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.entity.*;
import com.epam.user_service.exception.FieldAlreadyExistsException;
import com.epam.user_service.exception.FieldNotFoundException;
import com.epam.user_service.exception.UserException;
import com.epam.user_service.exception.UserNotFound;
import com.epam.user_service.repository.*;
import com.epam.user_service.service.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class AdminServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private AdminServiceImpl adminService;
    private Country country;
    private City city;
    private Category category;

    private FieldRequest fieldRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        country = new Country();
        country.setId(1);
        country.setName("USA");
        fieldRequest = new FieldRequest();
        fieldRequest.setName("Canada");
        city = new City();
        city.setId(1);
        city.setName("New York");
        fieldRequest = new FieldRequest();
        fieldRequest.setName("Los Angeles");
        MockitoAnnotations.initMocks(this);
        category = new Category();
        category.setId(1);
        category.setName("Electronics");
        fieldRequest = new FieldRequest();
        fieldRequest.setName("Books");
        openMocks(this);
        user = new User();
        user.setUsername("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCity("TestCity");
        user.setCountry("TestCountry");
        user.setIsEnabled(1);
        user.setPassword("password");

        adminUserDTO = new AdminUserDTO();
        adminUserDTO.setEmail("test@example.com");
        adminUserDTO.setFirstName("Test");
        adminUserDTO.setLastName("User");
        adminUserDTO.setCity("TestCity");
        adminUserDTO.setCountry("TestCountry");
        adminUserDTO.setPassword("newPassword");
    }

    @Test
    public void addCountry_success() {
        when(countryRepository.findByName(fieldRequest.getName())).thenReturn(Optional.empty());

        String response = adminService.addCountry(fieldRequest);

        assertEquals("Country added successfully.", response);
        ArgumentCaptor<Country> countryArgumentCaptor = ArgumentCaptor.forClass(Country.class);
        verify(countryRepository).save(countryArgumentCaptor.capture());
        assertEquals(fieldRequest.getName(), countryArgumentCaptor.getValue().getName());
    }


    @Test
    public void editCountry_success() {
        when(countryRepository.findById(country.getId())).thenReturn(Optional.of(country));

        String response = adminService.editCountry(country.getId(), fieldRequest);

        assertEquals("Country updated successfully.", response);
        verify(countryRepository).save(country);
        assertEquals(fieldRequest.getName(), country.getName());
    }

    @Test
    public void editCountry_notFound() {
        when(countryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(FieldNotFoundException.class, () -> adminService.editCountry(1, fieldRequest));
    }

    @Test
    public void removeCountry_success() {
        when(countryRepository.findById(country.getId())).thenReturn(Optional.of(country));
        doNothing().when(countryRepository).delete(country);

        String response = adminService.removeCountry(country.getId());

        assertEquals("Country removed successfully.", response);
    }

    @Test
    public void removeCountry_notFound() {
        when(countryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(FieldNotFoundException.class, () -> adminService.removeCountry(1));
    }

    @Test
    public void getAllCountries() {
        List<Country> countries = new ArrayList<>();
        countries.add(country);
        when(countryRepository.findAll()).thenReturn(countries);

        List<Country> fetchedCountries = adminService.getAllCountries();

        assertEquals(1, fetchedCountries.size());
        assertEquals("USA", fetchedCountries.get(0).getName());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCountryNotFound() {
        int countryId = 99;
        when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(FieldNotFoundException.class, () -> {
            adminService.toggleCountryStatus(countryId);
        });

        assertEquals("Country not found with id: " + countryId, exception.getMessage());
    }

    @Test
    public void testToggleCountryStatus_CountryFound_InitiallyDisabled() {
        // Arrange
        country.setEnabled(false);
        when(countryRepository.findById(1)).thenReturn(Optional.of(country));
        doNothing().when(countryRepository).toggleCountryStatus(1L);

        // Act
        String result = adminService.toggleCountryStatus(1);

        // Assert
        assertEquals("Country enabled successfully", result);
        verify(countryRepository).toggleCountryStatus(1L);
    }

    @Test
    public void addCity_success() {
        when(cityRepository.findByName(fieldRequest.getName())).thenReturn(Optional.empty());

        String response = adminService.addCity(fieldRequest);

        assertEquals("City added successfully.", response);
        ArgumentCaptor<City> cityArgumentCaptor = ArgumentCaptor.forClass(City.class);
        verify(cityRepository).save(cityArgumentCaptor.capture());
        assertEquals(fieldRequest.getName(), cityArgumentCaptor.getValue().getName());
    }


    @Test
    public void editCity_success() {
        when(cityRepository.findById(city.getId())).thenReturn(Optional.of(city));

        String response = adminService.editCity(city.getId(), fieldRequest);

        assertEquals("City updated successfully.", response);
        verify(cityRepository).save(city);
        assertEquals(fieldRequest.getName(), city.getName());
    }

    @Test
    public void editCity_notFound() {
        when(cityRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(FieldNotFoundException.class, () -> adminService.editCity(1, fieldRequest));
    }

    @Test
    public void removeCity_success() {
        when(cityRepository.findById(city.getId())).thenReturn(Optional.of(city));
        doNothing().when(cityRepository).delete(city);

        String response = adminService.removeCity(city.getId());

        assertEquals("City removed successfully.", response);
    }

    @Test
    public void removeCity_notFound() {
        when(cityRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(FieldNotFoundException.class, () -> adminService.removeCity(1));
    }

    @Test
    public void getAllCities() {
        List<City> cities = new ArrayList<>();
        cities.add(city);
        when(cityRepository.findAll()).thenReturn(cities);

        List<City> fetchedCities = adminService.getAllCities();

        assertEquals(1, fetchedCities.size());
        assertEquals("New York", fetchedCities.get(0).getName());
    }


    @Test
    void shouldThrowNotFoundExceptionWhenCityNotFound() {
        int cityId = 99;
        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(FieldNotFoundException.class, () -> {
            adminService.toggleCityStatus(cityId);
        });

        assertEquals("City not found with id: " + cityId, exception.getMessage());
    }
    @Test
    public void testToggleCityStatus_CityFound_InitiallyDisabled() {
        // Arrange
        city.setEnabled(false);
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        doNothing().when(cityRepository).toggleCityStatus(1L);

        // Act
        String result = adminService.toggleCityStatus(1);

        // Assert
        assertEquals("City enabled successfully", result);
        verify(cityRepository).toggleCityStatus(1L);
    }


   /* @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        category = new Category();
        category.setId(1);
        category.setName("Electronics");
        fieldRequest = new FieldRequest();
        fieldRequest.setName("Books");
        openMocks(this);
        user = new User();
        user.setUsername("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCity("TestCity");
        user.setCountry("TestCountry");
        user.setIsEnabled(1);
        user.setPassword("password");

        adminUserDTO = new AdminUserDTO();
        adminUserDTO.setEmail("test@example.com");
        adminUserDTO.setFirstName("Test");
        adminUserDTO.setLastName("User");
        adminUserDTO.setCity("TestCity");
        adminUserDTO.setCountry("TestCountry");
        adminUserDTO.setPassword("newPassword");
    }*/

    @Test
    public void addCategory_success() {
        when(categoryRepository.findByName(fieldRequest.getName())).thenReturn(Optional.empty());

        String response = adminService.addCategory(fieldRequest);

        assertEquals("Category added successfully.", response);
        ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryArgumentCaptor.capture());
        assertEquals(fieldRequest.getName(), categoryArgumentCaptor.getValue().getName());
    }

    @Test
    public void addCategory_alreadyExists() {
        when(categoryRepository.findByName("Books")).thenReturn(Optional.of(category));

        assertThrows(FieldAlreadyExistsException.class, () -> adminService.addCategory(fieldRequest));
    }

    @Test
    public void editCategory_success() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        String response = adminService.editCategory(category.getId(), fieldRequest);

        assertEquals("Category updated successfully.", response);
        verify(categoryRepository).save(category);
        assertEquals(fieldRequest.getName(), category.getName());
    }

    @Test
    public void editCategory_notFound() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(FieldNotFoundException.class, () -> adminService.editCategory(1, fieldRequest));
    }

    @Test
    public void removeCategory_success() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        String response = adminService.removeCategory(category.getId());

        assertEquals("Category removed successfully.", response);
    }

    @Test
    public void removeCategory_notFound() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(FieldNotFoundException.class, () -> adminService.removeCategory(1));
    }

    @Test
    public void getAllCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(category);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> fetchedCategories = adminService.getAllCategories();

        assertEquals(1, fetchedCategories.size());
        assertEquals("Electronics", fetchedCategories.get(0).getName());
    }
    @Test
    void shouldEnableCategorySuccessfully() {
        int categoryId = 1;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setEnabled(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        String result = adminService.toggleCategoryStatus(categoryId);

        verify(categoryRepository).toggleBlogStatus((long) categoryId);
        assertNotNull(result);
        assertTrue(result.contains("Category enabled successfully.") || result.contains("Category disabled successfully."));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        int categoryId = 99;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(FieldNotFoundException.class, () -> {
            adminService.toggleCategoryStatus(categoryId);
        });

        assertEquals("Category not found with id: " + categoryId, exception.getMessage());
    }
    @Test
    public void testToggleCategoryStatus_NotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(FieldNotFoundException.class, () -> {
            adminService.toggleCategoryStatus(1);
        });

    }

    @Test
    public void testToggleCategoryStatus_FoundAndEnabled() {
        // Arrange
        category.setEnabled(false);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).toggleBlogStatus(1L);

        // Act
        String result = adminService.toggleCategoryStatus(1);

        // Assert
        assertEquals("Category enabled successfully.", result);
        verify(categoryRepository).toggleBlogStatus(1L);
    }


    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private User user;
    private AdminUserDTO adminUserDTO;


    @Test
    void changeUserStatus_EnabledToDisabled() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        MessageResponse response = adminService.changeUserStatus("test@example.com");

        assertEquals("User Disabled", response.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void changeUserStatus_DisabledToEnabled() {
        user.setIsEnabled(0);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        MessageResponse response = adminService.changeUserStatus("test@example.com");

        assertEquals("User Enabled", response.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void changeUserStatus_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> adminService.changeUserStatus("test@example.com"));
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        MessageResponse response = adminService.updateUser(adminUserDTO, "test@example.com");

        assertEquals("User updated.", response.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> adminService.updateUser(adminUserDTO, "test@example.com"));
    }

    @Test
    void viewUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        AdminUserDTO result = adminService.viewUser("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByUsername("test@example.com");
    }

    @Test
    void viewUser_NotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> adminService.viewUser("test@example.com"));
    }

    @Test
    void viewAllUsers() {
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<AdminUserDTO> result = adminService.viewAllUsers(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void addAdminRole_Success() {
        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        MessageResponse response = adminService.addAdminRole("test@example.com");

        assertEquals("Admin role added to user", response.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void addAdminRole_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> adminService.addAdminRole("test@example.com"));
    }

    @Test
    void addAdminRole_RoleNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.addAdminRole("test@example.com"));
    }

}