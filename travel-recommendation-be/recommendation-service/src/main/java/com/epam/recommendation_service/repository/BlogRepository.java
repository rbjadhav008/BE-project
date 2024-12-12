package com.epam.recommendation_service.repository;


import com.epam.recommendation_service.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog,Integer> {
    Optional<Blog> getBlogById(Integer blogId);
    List<Blog> findAllByEmail(String email);

    @Query("SELECT b FROM Blog b WHERE b.status = 'APPROVED'")
    List<Blog> findAcceptedBlogs();

    Page<Blog> findByStatus(String status, Pageable pageable);

    @Query("SELECT b.status FROM Blog b WHERE b.id = :blogId")
    Optional<String> findStatusByBlogId(@Param("blogId") Integer blogId);


    @Modifying
    @Transactional
    @Query("UPDATE Blog b SET b.status = :status WHERE b.id = :blogId")
    void updateStatusByBlogId(@Param("blogId") Integer blogId, @Param("status") String status);

    @Query("SELECT b FROM Blog b WHERE b.status = :status")
    Optional<List<Blog>> findBlogsByStatus(@Param("status") String status);
}
