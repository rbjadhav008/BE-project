package com.epam.recommendation_service.service;

import com.epam.recommendation_service.dto.*;
import com.epam.recommendation_service.entity.Blog;
import com.epam.recommendation_service.exception.BlogNotFoundException;
import com.epam.recommendation_service.exception.InvalidBlogStatusException;
import com.epam.recommendation_service.exception.UnauthorizedBlogAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface BlogService {

    BlogResponse createBlog(BlogRequest blogRequest, String userEmail) throws IOException;
    BlogSummaryResponse getBlogById(Integer blogId) throws BlogNotFoundException;
    Page<BlogSummaryResponse> getBlogsByPage(int pageNo, int pageSize);
    List<BlogSummaryResponse> getAllBlogs(String email);
    MessageResponse deleteBlog(int blogId);
    List<BlogSummaryResponse> getMyBlogs(String email);
    Blog updateBlog(int id, BlogRequest blogRequest, String email) throws BlogNotFoundException, IOException, UnauthorizedBlogAccessException;
    String addFavorite(String email, int blogId) throws UnauthorizedBlogAccessException;
    String removeFavoriteBlog(String email,int blogId);
    List<Blog> getAll();
    List<Blog> filterBlogs(String category, String country, String query);
    MessageResponse addComment(String email,CommentRequest commentRequest,int blogId);
    List<CommentResponse> viewComments(int blogId,int pageNo,int pageSize);
    List<BlogSummaryResponse> getAllFavouriteBlogs(String userId);
    MessageResponse reportComment(String email,int commentId);

    MessageResponse modifyBlogStatus(String adminId, Integer blogId, String status) throws InvalidBlogStatusException;
    List<Blog> getApprovedBlogs();
    List<Blog> getRejectedBlogs();
    List<Blog> getPendingBlogs();
}
