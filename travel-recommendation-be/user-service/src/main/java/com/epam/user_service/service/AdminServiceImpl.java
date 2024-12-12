package com.epam.user_service.service;

import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.entity.Country;
import com.epam.user_service.exception.FieldAlreadyExistsException;
import com.epam.user_service.exception.FieldNotFoundException;
import com.epam.user_service.repository.CountryRepository;
import com.epam.user_service.entity.City;
import com.epam.user_service.exception.FieldAlreadyExistsException;
import com.epam.user_service.exception.FieldNotFoundException;
import com.epam.user_service.repository.CityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.entity.Category;
import com.epam.user_service.exception.FieldAlreadyExistsException;
import com.epam.user_service.exception.FieldNotFoundException;
import com.epam.user_service.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.epam.user_service.dto.*;
import com.epam.user_service.entity.ERole;
import com.epam.user_service.entity.Role;
import com.epam.user_service.entity.User;
import com.epam.user_service.exception.UserAlreadyExistsException;
import com.epam.user_service.exception.UserException;
import com.epam.user_service.exception.UserNotFound;
import com.epam.user_service.repository.RoleRepository;
import com.epam.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public String addCountry(FieldRequest fieldRequest) {
        Optional<Country> optionalCountry = countryRepository.findByName(fieldRequest.getName());
        if (optionalCountry.isPresent()) {
            throw new FieldAlreadyExistsException("This country already exists");
        }

        Country newCountry = new Country();
        newCountry.setName(fieldRequest.getName());
        newCountry.setEnabled(false);
        countryRepository.save(newCountry);
        return "Country added successfully.";
    }

    @Override
    public String editCountry(int id, FieldRequest fieldRequest) {
        return countryRepository.findById(id)
                .map(existingCountry -> {
                    existingCountry.setName(fieldRequest.getName());
                    countryRepository.save(existingCountry);
                    return "Country updated successfully.";
                })
                .orElseThrow(() -> new FieldNotFoundException("Country not found with id: " + id));
    }

    @Override
    public String removeCountry(int id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException("Country not found with id: " + id));
        countryRepository.delete(country);
        return "Country removed successfully.";
    }

    @Override
    public List<Country> getAllCountries() {
        try {
            return countryRepository.findAll();
        } catch (DataAccessException e) {
            throw new FieldNotFoundException("Failed to retrieve countries: " + e.getMessage());

        }
    }
    @Autowired
    private CityRepository cityRepository;

    @Override
    public String addCity(FieldRequest fieldRequest) {
        Optional<City> optionalCity = cityRepository.findByName(fieldRequest.getName());
        if (optionalCity.isPresent()) {
            throw new FieldAlreadyExistsException("This city already exists");
        }

        City newCity = new City();
        newCity.setName(fieldRequest.getName());
        newCity.setEnabled(false);
        cityRepository.save(newCity);
        return "City added successfully.";
    }

    public String editCity(int id, FieldRequest fieldRequest) {
        return cityRepository.findById(id)
                .map(existingCity -> {
                    existingCity.setName(fieldRequest.getName());
                    cityRepository.save(existingCity);
                    return "City updated successfully.";
                })
                .orElseThrow(() -> new FieldNotFoundException("City not found with id: " + id));
    }

    @Override
    public String removeCity(int id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException("City not found with id: " + id));
        cityRepository.delete(city);
        return "City removed successfully.";
    }

    @Override
    public List<City> getAllCities() {
        try {
            return cityRepository.findAll();
        } catch (DataAccessException e) {
            throw new FieldNotFoundException("Failed to retrieve cities: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public String toggleCountryStatus(int id) {
        countryRepository.toggleCountryStatus((long) id);
        boolean status = countryRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException("Country not found with id: " + id))
                .isEnabled();
        return status ? "Country disabled successfully." : "Country enabled successfully";
    }
    public String toggleCityStatus(int id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException("City not found with id: " + id));
        cityRepository.toggleCityStatus((long) id);
        boolean status = city.isEnabled();
        return status ? "City disabled successfully." : "City enabled successfully";
    }
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public String addCategory(FieldRequest fieldRequest) {

        Optional<Category> optionalCategory = categoryRepository.findByName(fieldRequest.getName());
        if(optionalCategory.isPresent()){
            throw new FieldAlreadyExistsException("this category already exist");
        }

        Category newCategory = new Category();
        newCategory.setName(fieldRequest.getName());
        categoryRepository.save(newCategory);
        return "Category added successfully.";

    }

    public String editCategory(int id, FieldRequest fieldRequest) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(fieldRequest.getName());
                    categoryRepository.save(existingCategory);
                    return "Category updated successfully.";
                })
                .orElseThrow(() -> new FieldNotFoundException("Category not found with name: " + id));
    }

    @Override
    public String removeCategory(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
        return "Category removed successfully.";
    }

    @Override
    public List<Category> getAllCategories() {
        try {
            return categoryRepository.findAll();
        } catch (DataAccessException e) {
            throw new FieldNotFoundException("Failed to retrieve categories: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public String toggleCategoryStatus(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException("Category not found with id: " + id));
        categoryRepository.toggleBlogStatus((long) id);
        boolean status = category.isEnabled();
        return status ? "Category disabled successfully." : "Category enabled successfully.";
    }
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;


    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public MessageResponse changeUserStatus(String email) {
        User user = userRepository.findByUsername(email).orElseThrow(() -> new UserException("User not found with email "+email, HttpStatus.NOT_FOUND));
        if(user.getIsEnabled() == 1) {
            user.setIsEnabled(0);
            userRepository.save(user);
            return new MessageResponse("User Disabled");
        }
        user.setIsEnabled(1);
        userRepository.save(user);
        return new MessageResponse("User Enabled");
    }

    @Override
    public MessageResponse updateUser(AdminUserDTO adminUserDTO, String email) throws IOException {
        User user=userRepository.findByUsername(email).orElseThrow(()-> new UserException("User not found with email " + email,HttpStatus.NOT_FOUND));
        user.setFirstName(adminUserDTO.getFirstName());
        user.setLastName(adminUserDTO.getLastName());
        user.setCity(adminUserDTO.getCity());
        user.setCountry(adminUserDTO.getCountry());
        setPassword(adminUserDTO, user);
        if(adminUserDTO.getProfileImage() != null && !adminUserDTO.getProfileImage().isEmpty()) {
            user.setProfileImage(adminUserDTO.getProfileImage().getBytes());
        }

        if (!adminUserDTO.getEmail().equals(email) && userRepository.existsByUsername(adminUserDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email " + adminUserDTO.getEmail() + " is already in use.");
        }
        user.setUsername(adminUserDTO.getEmail());
        userRepository.save(user);
        return new MessageResponse("User updated.");
    }

    @Override
    public AdminUserDTO viewUser(String email) {
        User user=userRepository.findByUsername(email).orElseThrow(()-> new UserException("User not found with email " + email,HttpStatus.NOT_FOUND));
        return mapToAdminUserDTO(user);
    }

    @Override
    public Page<AdminUserDTO> viewAllUsers(int pageNo,int pageSize) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(pageNo,pageSize));
        List<AdminUserDTO> adminUserDTOList = userPage.getContent().stream()
                .map(this::mapToAdminUserDTO).collect(Collectors.toList());

        return new PageImpl<>(adminUserDTOList,userPage.getPageable(),userPage.getTotalElements());
    }

    public MessageResponse addAdminRole(String email){
        User user = userRepository.findByUsername(email).orElseThrow(()-> new UserNotFound("User not found with email: "+email));
        user.getRoles().add(roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found")));

        userRepository.save(user);
        return new MessageResponse("Admin role added to user");
    }


    private AdminUserDTO mapToAdminUserDTO(User user){
        AdminUserDTO adminUserDTO = new AdminUserDTO();
        adminUserDTO.setEmail(user.getUsername());
        adminUserDTO.setFirstName(user.getFirstName());
        adminUserDTO.setLastName(user.getLastName());
        adminUserDTO.setCity(user.getCity());
        adminUserDTO.setCountry(user.getCountry());
        adminUserDTO.setImageURL(getImageURL(user));
        adminUserDTO.setPassword("********");
        adminUserDTO.setStatus(user.getIsEnabled() == 1 ? "Enabled" : "Disabled");

        Set<Role> roles = user.getRoles();
        String roleToSet = roles.stream()
                .map(Role::getName) // This will map to ERole values
                .anyMatch(roleName -> roleName == ERole.ROLE_ADMIN) ? "ADMIN" : "USER"; // Check if any role is ROLE_ADMIN
        adminUserDTO.setRole(roleToSet);

        return adminUserDTO;
    }


    private void setPassword(AdminUserDTO adminUserDTO, User user) {
        if (adminUserDTO.getPassword()!=null && !adminUserDTO.getPassword().isEmpty()) {
            if(adminUserDTO.getPassword().length() < 7){
                throw new UserException("Password length should be greater than 7",HttpStatus.BAD_REQUEST);
            }else {
                user.setPassword(passwordEncoder.encode(adminUserDTO.getPassword()));
            }
        }
    }

    private static String getImageURL(User user) {
        if(user.getProfileImage() == null){
            return "https://yourteachingmentor.com/wp-content/uploads/2020/12/istockphoto-1223671392-612x612-1.jpg";
        }
        return "data:" + getMimeType(user.getProfileImage()) + ";base64," + Base64.getEncoder().encodeToString(user.getProfileImage());
    }

    public static String getMimeType(byte[] imageData) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(imageData)) {
            return URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            System.err.println("Error determining MIME type: " + e.getMessage());
            return "application/octet-stream";
        }
    }

}