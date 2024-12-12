package com.epam.user_service.repository;
import org.springframework.stereotype.Repository;
import com.epam.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String email);


}
