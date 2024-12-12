package com.epam.recommendation_service.repository;

import com.epam.recommendation_service.entity.FavouriteBlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavouriteBlogRepository extends JpaRepository<FavouriteBlog,Integer> {

    Optional<FavouriteBlog> findByEmail(String email);
}

