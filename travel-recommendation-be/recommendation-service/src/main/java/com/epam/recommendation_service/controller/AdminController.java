package com.epam.recommendation_service.controller;

import com.epam.recommendation_service.dto.BlogStatusRequest;
import com.epam.recommendation_service.dto.BlogSummaryResponse;
import com.epam.recommendation_service.dto.MessageResponse;
import com.epam.recommendation_service.entity.Blog;
import com.epam.recommendation_service.exception.BlogNotFoundException;
import com.epam.recommendation_service.exception.InvalidBlogStatusException;
import com.epam.recommendation_service.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/blogs")
@RequiredArgsConstructor
public class AdminController {

    private final BlogService blogService;

    @PutMapping("/status/{blogId}")
    public ResponseEntity<MessageResponse> setBlogStatus
            (@RequestHeader("X-User-Id") String adminId,
             @PathVariable Integer blogId,
            @Valid @RequestBody BlogStatusRequest blogStatusRequest)
            throws InvalidBlogStatusException

    {
        String status =blogStatusRequest.getStatus();

        MessageResponse response= blogService.modifyBlogStatus(adminId,blogId, status);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/get/{id}")
    public ResponseEntity<BlogSummaryResponse> getBlogByIdForAdmin(@PathVariable("id") Integer blogId) throws BlogNotFoundException {
        BlogSummaryResponse blogDetails = blogService.getBlogById(blogId);
        return ResponseEntity.ok(blogDetails);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<Blog>> getApprovedBlogs() {
        List<Blog> blogs = blogService.getApprovedBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<Blog>> getRejectedBlogs() {
        List<Blog> blogs = blogService.getRejectedBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Blog>> getPendingBlogs() {
        List<Blog> blogs = blogService.getPendingBlogs();
        return ResponseEntity.ok(blogs);
    }


}
