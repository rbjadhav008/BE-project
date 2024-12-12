package com.epam.recommendation_service.controller;

import com.epam.recommendation_service.dto.*;
import com.epam.recommendation_service.entity.Blog;
import com.epam.recommendation_service.exception.BlogNotFoundException;
import com.epam.recommendation_service.exception.InvalidBlogStatusException;
import com.epam.recommendation_service.exception.UnauthorizedBlogAccessException;
import com.epam.recommendation_service.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlogControllerTest {

    @Mock
    private BlogService blogService;

    @InjectMocks
    private BlogController blogController;

    @Mock
    private List<Blog> mockBlogs;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void createBlog_Success() throws IOException {
        BlogRequest blogRequest = new BlogRequest();
        BlogResponse blogResponse = new BlogResponse();
        when(blogService.createBlog(blogRequest, "user1")).thenReturn(blogResponse);

        ResponseEntity<BlogResponse> response = blogController.createBlog(blogRequest, "user1");

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        verify(blogService).createBlog(blogRequest, "user1");
    }

    @Test
    void getMyBlogs_Found() {
        List<BlogSummaryResponse> blogSummaryResponses = Arrays.asList(new BlogSummaryResponse(), new BlogSummaryResponse());
        when(blogService.getMyBlogs("user1")).thenReturn(blogSummaryResponses);

        ResponseEntity<List<BlogSummaryResponse>> response = blogController.getMyBlogs("user1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getMyBlogs_NotFound() {
        when(blogService.getMyBlogs("user1")).thenReturn(Collections.emptyList());

        ResponseEntity<List<BlogSummaryResponse>> response = blogController.getMyBlogs("user1");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addFavouriteBlog_Success() throws UnauthorizedBlogAccessException {
        when(blogService.addFavorite("user1", 1)).thenReturn("favorite blog added successfully");

        ResponseEntity<MessageResponse> response = blogController.addFavouriteBlog("user1", 1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("favorite blog added successfully", response.getBody().getMessage());
    }

    @Test
    void removeFavouriteBlog_Success() {
        when(blogService.removeFavoriteBlog("user1", 1)).thenReturn("favorite blog removed successfully");

        ResponseEntity<MessageResponse> response = blogController.removeFavouriteBlog("user1", 1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("favorite blog removed successfully", response.getBody().getMessage());
    }

    @Test
    void getBlogById_Success() throws BlogNotFoundException {
        BlogSummaryResponse blogSummaryResponse = new BlogSummaryResponse();
        when(blogService.getBlogById(1)).thenReturn(blogSummaryResponse);

        ResponseEntity<BlogSummaryResponse> response = blogController.getBlogById(1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getAllBlogs_Success() {
        List<BlogSummaryResponse> blogSummaryResponses = Arrays.asList(new BlogSummaryResponse(), new BlogSummaryResponse());
        when(blogService.getAllBlogs(anyString())).thenReturn(blogSummaryResponses);

        List<BlogSummaryResponse> response = blogController.getAllBlogs("");

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void deleteBlogById_Success() {
        MessageResponse messageResponse = new MessageResponse("Blog deleted successfully");
        when(blogService.deleteBlog(1)).thenReturn(messageResponse);

        MessageResponse response = blogController.deleteBlogById(1).getBody();

        assertNotNull(response);
        assertEquals("Blog deleted successfully", response.getMessage());
    }

    @Test
    void updateBlogById_Success() throws BlogNotFoundException, IOException, UnauthorizedBlogAccessException {
        BlogRequest blogRequest = new BlogRequest();
        Blog blog = new Blog();
        when(blogService.updateBlog(1, blogRequest, "user1")).thenReturn(blog);

        ResponseEntity<Blog> response = blogController.updateBlogById(1, blogRequest, "user1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(blog, response.getBody());
    }
    @Test
    void testFilterBlogsByQuery() {

        Blog blog2 = new Blog(2, "Blog 2", "Description 2", null, "Category 2", "City 2", "Country 2", "email2@example.com", LocalDateTime.now(), "Author 2", "PUBLISHED",new ArrayList<>());
        List<Blog> filteredBlogs = Arrays.asList(blog2);
        when(blogService.filterBlogs(null, null, "description 2")).thenReturn(filteredBlogs);

        ResponseEntity<List<Blog>> response = blogController.filterBlogs(null, null, "description 2");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Blog 2", response.getBody().get(0).getTitle());
    }

    @Test
    void testFilterBlogsByCategory() {
        Blog blog1 = new Blog(1, "Blog 1", "Description 1", null, "Category 1", "City 1", "Country 1", "email1@example.com", LocalDateTime.now(), "Author 1", "PENDING",new ArrayList<>());
        Blog blog2 = new Blog(2, "Blog 2", "Description 2", null, "Category 2", "City 2", "Country 2", "email2@example.com", LocalDateTime.now(), "Author 2", "PUBLISHED",new ArrayList<>());
        Blog blog3 = new Blog(3, "Blog 3", "Description 3", null, "Category 2", "City 3", "Country 3", "email3@example.com", LocalDateTime.now(), "Author 3", "PENDING",new ArrayList<>());
        List<Blog> filteredBlogs = Arrays.asList(blog2, blog3);
        when(blogService.filterBlogs("Category 2", null, null)).thenReturn(filteredBlogs);

        ResponseEntity<List<Blog>> response = blogController.filterBlogs("Category 2", null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Blog 2", response.getBody().get(0).getTitle());
        assertEquals("Blog 3", response.getBody().get(1).getTitle());
    }

    @Test
    void testFilterBlogsByCountry() {
        Blog blog1 = new Blog(1, "Blog 1", "Description 1", null, "Category 1", "City 1", "Country 1", "email1@example.com", LocalDateTime.now(), "Author 1", "PENDING",new ArrayList<>());
        Blog blog2 = new Blog(2, "Blog 2", "Description 2", null, "Category 2", "City 2", "Country 2", "email2@example.com", LocalDateTime.now(), "Author 2", "PUBLISHED",new ArrayList<>());
        Blog blog3 = new Blog(3, "Blog 3", "Description 3", null, "Category 3", "City 3", "Country 2", "email3@example.com", LocalDateTime.now(), "Author 3", "PENDING",new ArrayList<>());
        List<Blog> filteredBlogs = Arrays.asList(blog2, blog3);
        when(blogService.filterBlogs(null, "Country 2", null)).thenReturn(filteredBlogs);

        ResponseEntity<List<Blog>> response = blogController.filterBlogs(null, "Country 2", null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Blog 2", response.getBody().get(0).getTitle());
        assertEquals("Blog 3", response.getBody().get(1).getTitle());
    }


    @Test
    void getFavouriteBlogs_Success() {
        List<BlogSummaryResponse> favouriteBlogs = Arrays.asList(new BlogSummaryResponse(), new BlogSummaryResponse());
        when(blogService.getAllFavouriteBlogs("user1")).thenReturn(favouriteBlogs);

        ResponseEntity<List<BlogSummaryResponse>> response = blogController.getFavouriteBlogs("user1");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(blogService).getAllFavouriteBlogs("user1");
    }

    @Test
    void getFavouriteBlogs_NotFound() {
        when(blogService.getAllFavouriteBlogs("user1")).thenReturn(Collections.emptyList());


        ResponseEntity<List<BlogSummaryResponse>> response = blogController.getFavouriteBlogs("user1");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() == null || response.getBody().isEmpty());
        verify(blogService).getAllFavouriteBlogs("user1");
    }

    @Test
    void testSetBlogStatus_Success() throws InvalidBlogStatusException {
        String adminId = "admin@example.com";
        Integer blogId = 1;
        String status = "APPROVED";

        BlogStatusRequest blogStatusRequest = new BlogStatusRequest(status);

        MessageResponse expectedResponse = new MessageResponse("Blog status has been successfully set to APPROVED");
        when(blogService.modifyBlogStatus(adminId, blogId, status)).thenReturn(expectedResponse);

        ResponseEntity<MessageResponse> response = adminController.setBlogStatus(adminId, blogId, blogStatusRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse.getMessage(), response.getBody().getMessage());
        verify(blogService, times(1)).modifyBlogStatus(adminId, blogId, status);
    }





    @Test
    void testSetBlogStatus_InvalidStatus() throws InvalidBlogStatusException {
        String adminId = "admin@example.com";
        Integer blogId = 1;
        String invalidStatus = "INVALID_STATUS";

        BlogStatusRequest blogStatusRequest = new BlogStatusRequest(invalidStatus);

        doThrow(new InvalidBlogStatusException("Invalid status provided: " + invalidStatus))
                .when(blogService).modifyBlogStatus(adminId, blogId, invalidStatus);

        InvalidBlogStatusException exception = assertThrows(
                InvalidBlogStatusException.class,
                () -> adminController.setBlogStatus(adminId, blogId, blogStatusRequest)
        );

        assertEquals("Invalid status provided: INVALID_STATUS", exception.getMessage());
        verify(blogService, times(1)).modifyBlogStatus(adminId, blogId, invalidStatus);
    }

    private Blog createBlog(int id, String title, String status, String email) {
        Blog blog = new Blog();
        blog.setId(id);
        blog.setTitle(title);
        blog.setStatus(status);
        blog.setEmail(email);
        blog.setCreationTime(LocalDateTime.now());
        blog.setAuthorName("Author Name");
        return blog;
    }

    @Test
    void getApprovedBlogs_ShouldReturnApprovedBlogs() {
        // Arrange
        Blog blog1 = createBlog(1, "Approved Blog 1", "APPROVED", "author1@example.com");
        Blog blog2 = createBlog(2, "Approved Blog 2", "APPROVED", "author2@example.com");
        when(blogService.getApprovedBlogs()).thenReturn(Arrays.asList(blog1, blog2));

        // Act
        ResponseEntity<List<Blog>> response = adminController.getApprovedBlogs();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getBody().size());
        assertEquals("Approved Blog 1", response.getBody().get(0).getTitle());
        verify(blogService, times(1)).getApprovedBlogs();
    }

    @Test
    void getRejectedBlogs_ShouldReturnRejectedBlogs() {
        // Arrange
        Blog blog1 = createBlog(3, "Rejected Blog 1", "REJECTED", "author3@example.com");
        when(blogService.getRejectedBlogs()).thenReturn(Arrays.asList(blog1));

        // Act
        ResponseEntity<List<Blog>> response = adminController.getRejectedBlogs();

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals("Rejected Blog 1", response.getBody().get(0).getTitle());
        verify(blogService, times(1)).getRejectedBlogs();
    }

    @Test
    void getPendingBlogs_ShouldReturnPendingBlogs() {
        // Arrange
        Blog blog1 = createBlog(4, "Pending Blog 1", "PENDING", "author4@example.com");
        Blog blog2 = createBlog(5, "Pending Blog 2", "PENDING", "author5@example.com");
        when(blogService.getPendingBlogs()).thenReturn(Arrays.asList(blog1, blog2));

        // Act
        ResponseEntity<List<Blog>> response = adminController.getPendingBlogs();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getBody().size());
        assertEquals("Pending Blog 1", response.getBody().get(0).getTitle());
        verify(blogService, times(1)).getPendingBlogs();
    }
}