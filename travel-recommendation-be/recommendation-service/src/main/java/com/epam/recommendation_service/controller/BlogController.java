package com.epam.recommendation_service.controller;

import com.epam.recommendation_service.dto.*;
import com.epam.recommendation_service.entity.Blog;
import com.epam.recommendation_service.exception.BlogNotFoundException;
import com.epam.recommendation_service.exception.UnauthorizedBlogAccessException;
import com.epam.recommendation_service.service.BlogService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Validated
public class BlogController {
    private final BlogService blogService;
    @PostMapping("/create")
    public ResponseEntity<BlogResponse> createBlog(@Valid @ModelAttribute BlogRequest blogRequest, @RequestHeader("X-User-Id") String userId) throws IOException {
        BlogResponse blogResponse = blogService.createBlog(blogRequest, userId);
        return new ResponseEntity<>(blogResponse, HttpStatus.CREATED);
    }
    @GetMapping("/myBlogs")
    public ResponseEntity<List<BlogSummaryResponse>> getMyBlogs (@RequestHeader("X-User-Id") String userId) {
        List<BlogSummaryResponse> blogSummary = blogService.getMyBlogs(userId);
        if (blogSummary == null || blogSummary.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(blogSummary);
    }

    @PostMapping("addFavourite/{blogId}")
    public ResponseEntity<MessageResponse> addFavouriteBlog(@RequestHeader("X-User-Id") String userId, @PathVariable("blogId") int blogId) throws UnauthorizedBlogAccessException {
        String result = blogService.addFavorite(userId, blogId);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @DeleteMapping("removeFavourite/{blogId}")
    public ResponseEntity<MessageResponse> removeFavouriteBlog(@RequestHeader("X-User-Id") String userId, @PathVariable("blogId") int blogId) {
        String result = blogService.removeFavoriteBlog(userId, blogId);
        return ResponseEntity.ok(new MessageResponse(result));
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<BlogSummaryResponse> getBlogById(@PathVariable("id") Integer blogId) throws BlogNotFoundException {
        BlogSummaryResponse blogDetails = blogService.getBlogById(blogId);
        return ResponseEntity.ok(blogDetails);
    }

    @GetMapping("allBlogs")
    public List<BlogSummaryResponse> getAllBlogs(@RequestHeader("X-User-Id") String email){
        return blogService.getAllBlogs(email);
    }

    @PatchMapping("delete/{blogId}")
    public ResponseEntity<MessageResponse> deleteBlogById (@PathVariable int blogId){

        return ResponseEntity.ok(blogService.deleteBlog(blogId));
    }
    @PutMapping("update/{id}")
    public ResponseEntity<Blog> updateBlogById (@PathVariable("id") int id, @ModelAttribute BlogRequest blogRequest, @RequestHeader("X-User-Id") String email) throws BlogNotFoundException, IOException, UnauthorizedBlogAccessException {
        Blog updatedBlog = blogService.updateBlog(id, blogRequest,email);
        return ResponseEntity.ok(updatedBlog);
    }
    @GetMapping("/filter")
    public ResponseEntity<List<Blog>> filterBlogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String query) {

        List<Blog> filteredBlogs = blogService.filterBlogs(category, country, query);
        return ResponseEntity.ok(filteredBlogs);
            }

    @PostMapping("addComment/{blogId}")
    public ResponseEntity<MessageResponse> addComment(@RequestHeader("X-User-Id") String email, @ModelAttribute @Valid CommentRequest commentRequest, @PathVariable int blogId){
        return ResponseEntity.ok(blogService.addComment(email,commentRequest,blogId));
    }

    @GetMapping("{blogId}/comments")
    public List<CommentResponse> getComments(@PathVariable int blogId,
                                             @RequestParam(defaultValue = "0") int pageNo,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return blogService.viewComments(blogId, pageNo, pageSize);}

    @GetMapping("getAllFavouriteBlogs")
    public ResponseEntity<List<BlogSummaryResponse>> getFavouriteBlogs(@RequestHeader("X-User-Id") String userId){
        List<BlogSummaryResponse> blogSummary = blogService.getAllFavouriteBlogs(userId);
        if (blogSummary == null || blogSummary.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(blogSummary);
    }

    @PostMapping("report/{commentID}")
    public MessageResponse reportComment(@RequestHeader("X-User-Id") String email, @PathVariable int commentID){
        return blogService.reportComment(email,commentID);
    }
}
