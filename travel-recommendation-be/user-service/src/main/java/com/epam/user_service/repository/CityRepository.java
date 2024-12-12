package com.epam.user_service.repository;

import com.epam.user_service.entity.City;
import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByName(String name);
    @Transactional
    @Modifying
    @Query("UPDATE City c SET c.isEnabled = NOT c.isEnabled WHERE c.id = :cityId")
    void toggleCityStatus(@Param("cityId") Long cityId);
}