package com.epam.user_service.service;

import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.entity.Country;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.entity.City;
import java.util.List;
import com.epam.user_service.dto.AdminUserDTO;
import com.epam.user_service.dto.FieldRequest;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.entity.Category;
import com.epam.user_service.dto.AdminUserDTO;
import java.util.List;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.entity.User;
import org.springframework.data.domain.Page;
import java.io.IOException;
public interface AdminService {
    String addCountry(FieldRequest fieldRequest);
    String editCountry(int id, FieldRequest fieldRequest);
    String removeCountry(int id);
    List<Country> getAllCountries();
    String toggleCountryStatus(int id);
    String addCity(FieldRequest fieldRequest);
    String editCity(int id, FieldRequest fieldRequest);
    String removeCity(int id);
    List<City> getAllCities();
    String toggleCityStatus(int id);
    String addCategory(FieldRequest fieldRequest);
    String editCategory(int id, FieldRequest fieldRequest);
    String removeCategory(int id);
    List<Category> getAllCategories();
    String toggleCategoryStatus(int id);
    MessageResponse changeUserStatus(String email);
    MessageResponse updateUser(AdminUserDTO adminUserDTO,String email) throws IOException;
    AdminUserDTO viewUser(String email);
    Page<AdminUserDTO> viewAllUsers(int pageNo,int pageSize);
    MessageResponse addAdminRole(String email);

}