package com.epam.user_service.repository;

import com.epam.user_service.entity.Country;
import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    Optional<Country> findByName(String name);
    @Transactional
    @Modifying
    @Query("UPDATE Country c SET c.isEnabled = NOT c.isEnabled WHERE c.id = :countryId")
    void toggleCountryStatus(@Param("countryId") Long countryId);
}