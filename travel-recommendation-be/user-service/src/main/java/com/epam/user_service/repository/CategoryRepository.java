package com.epam.user_service.repository;

import com.epam.user_service.entity.Category;
import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Integer> {
    Optional<Category> findByName(String name);
    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.isEnabled = NOT c.isEnabled WHERE c.id = :categoryId")
    void toggleBlogStatus(@Param("categoryId") Long categoryId);
}
