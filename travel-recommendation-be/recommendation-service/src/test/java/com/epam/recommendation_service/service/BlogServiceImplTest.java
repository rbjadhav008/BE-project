package com.epam.recommendation_service.service;

import com.epam.recommendation_service.client.UserClient;
import com.epam.recommendation_service.dto.*;
import com.epam.recommendation_service.entity.*;

import com.epam.recommendation_service.entity.Blog;
import com.epam.recommendation_service.entity.Comment;
import com.epam.recommendation_service.entity.FavouriteBlog;
import com.epam.recommendation_service.entity.User;
import com.epam.recommendation_service.exception.*;
import com.epam.recommendation_service.repository.BlogRepository;
import com.epam.recommendation_service.repository.CommentRepository;
import com.epam.recommendation_service.repository.FavouriteBlogRepository;
import com.epam.recommendation_service.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BlogServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private FavouriteBlogRepository favouriteBlogRepository;

    @Mock
    private UserClient userClient;
    @Mock
    private List<Blog> mockBlogs;

    @InjectMocks
    private BlogServiceImpl blogService;
    private List<Blog> blogs;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Blog blog1 = new Blog(1, "Blog 1", "Description 1", null, "Category 1", "City 1", "Country 1", "email1@example.com", LocalDateTime.now(), "Author 1", "PENDING",new ArrayList<>());
        Blog blog2 = new Blog(2, "Blog 2", "Description 2", null, "Category 2", "City 2", "Country 2", "email2@example.com", LocalDateTime.now(), "Author 2", "PUBLISHED",new ArrayList<>());
        Blog blog3 = new Blog(3, "Blog 3", "Description 3", null, "Category 2", "City 3", "Country 3", "email3@example.com", LocalDateTime.now(), "Author 3", "PENDING",new ArrayList<>());
        Blog blog4 = new Blog(4, "Blog 4", "Description 4", null, "Category 3", "City 4", "Country 2", "email4@example.com", LocalDateTime.now(), "Author 4", "PUBLISHED",new ArrayList<>());

        blogs = Arrays.asList(blog1, blog2, blog3, blog4);
    }

    @Test
    void createBlog_Success() throws IOException {
        BlogRequest blogRequest = new BlogRequest();
        blogRequest.setTitle("Test Blog");
        blogRequest.setDescription("Test Description");

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe@example.com");

        when(userClient.getUserByEmail(anyString())).thenReturn(user);
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogResponse blogResponse = blogService.createBlog(blogRequest, "john.doe@example.com");

        assertNotNull(blogResponse);
        assertEquals("Test Blog", blogResponse.getTitle());
        assertEquals("Test Description", blogResponse.getDescription());
        verify(blogRepository).save(any(Blog.class));
    }

    @Test
    void getBlogById_NotFound() {
        when(blogRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(BlogNotFoundException.class, () -> blogService.getBlogById(1));
    }

    @Test
    void getBlogsByPage_Success() {
        // Arrange
        Blog blog = new Blog();
        blog.setTitle("Test Blog");
        blog.setEmail("test@gmail.com");
        Page<Blog> page = new PageImpl<>(Collections.singletonList(blog));

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName("John");
        userProfileResponse.setLastName("Doe");
        userProfileResponse.setImageURL("http://example.com/image.jpg");

        when(blogRepository.findByStatus(eq("APPROVED"), any(PageRequest.class))).thenReturn(page);
        when(userClient.getUserProfileByEmail("test@gmail.com")).thenReturn(userProfileResponse);

        // Act
        Page<BlogSummaryResponse> result = blogService.getBlogsByPage(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Blog", result.getContent().get(0).getTitle());
        assertEquals("John Doe", result.getContent().get(0).getAuthor());
        assertEquals("http://example.com/image.jpg", result.getContent().get(0).getAuthorImageURL());
    }

    @Test
    void deleteBlog_Success() {
        Blog blog = new Blog();
        blog.setId(1);
        blog.setTitle("Test Blog");

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = blogService.deleteBlog(1);

        assertNotNull(response);
        assertEquals("Blog deleted", response.getMessage());
        verify(blogRepository).save(blog);
    }

    @Test
    void deleteBlog_NotFound() {
        when(blogRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(BlogNotFoundException.class, () -> blogService.deleteBlog(1));
    }

    @Test
    void updateBlog_Success() throws IOException, UnauthorizedBlogAccessException {
        BlogRequest blogRequest = new BlogRequest();
        blogRequest.setTitle("Updated Title");
        blogRequest.setDescription("Updated Description");

        Blog blog = new Blog();
        blog.setId(1);
        blog.setTitle("Original Title");
        blog.setDescription("Original Description");
        blog.setEmail("john.doe@example.com");

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Blog updatedBlog = blogService.updateBlog(1, blogRequest, "john.doe@example.com");

        assertNotNull(updatedBlog);
        assertEquals("Updated Title", updatedBlog.getTitle());
        assertEquals("Updated Description", updatedBlog.getDescription());
        verify(blogRepository).save(blog);
    }

    @Test
    void updateBlog_NotFound() {
        BlogRequest blogRequest = new BlogRequest();

        when(blogRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(BlogNotFoundException.class, () -> blogService.updateBlog(1, blogRequest, "john.doe@example.com"));
    }

    @Test
    void addFavorite_Success() throws UnauthorizedBlogAccessException {
        Blog blog = new Blog();
        blog.setId(1);
        blog.setStatus("APPROVED");

        FavouriteBlog favouriteBlog = new FavouriteBlog();
        favouriteBlog.setEmail("john.doe@example.com");
        favouriteBlog.setFavoriteBlogs(new ArrayList<>());  // Initialize the favorite blogs set as empty

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(favouriteBlogRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(favouriteBlog));
        when(favouriteBlogRepository.save(any(FavouriteBlog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = blogService.addFavorite("john.doe@example.com", 1);

        assertEquals("favorite blog added successfully", result);
        verify(favouriteBlogRepository).save(favouriteBlog);
    }

    @Test
    void addFavorite_BlogNotFound() {
        when(blogRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BlogNotFoundException.class, () -> blogService.addFavorite("john.doe@example.com", 1));
    }

    @Test
    void addFavorite_BlogAlreadyFavorited() {
        Blog blog = new Blog();
        blog.setId(1);
        blog.setStatus("APPROVED");

        FavouriteBlog favouriteBlog = new FavouriteBlog();
        favouriteBlog.setEmail("john.doe@example.com");
        favouriteBlog.addFavoriteBlog(blog);

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(favouriteBlogRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(favouriteBlog));

        assertThrows(BlogAlreadyPresentException.class, () -> blogService.addFavorite("john.doe@example.com", 1));
    }

    @Test
    void removeFavoriteBlog_Success() {
        Blog blog = new Blog();
        blog.setId(1);

        FavouriteBlog favouriteBlog = new FavouriteBlog();
        favouriteBlog.setEmail("john.doe@example.com");
        favouriteBlog.addFavoriteBlog(blog);

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(favouriteBlogRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(favouriteBlog));
        when(favouriteBlogRepository.save(any(FavouriteBlog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = blogService.removeFavoriteBlog("john.doe@example.com", 1);

        assertEquals("Favorite blog removed successfully", result);
        verify(favouriteBlogRepository).save(favouriteBlog);
    }

    @Test
    void removeFavoriteBlog_NotFound() {
        when(blogRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BlogNotFoundException.class, () -> blogService.removeFavoriteBlog("john.doe@example.com", 1));
    }

    @Test
    void removeFavoriteBlog_NotFavorited() {
        Blog blog = new Blog();
        blog.setId(1);

        FavouriteBlog favouriteBlog = new FavouriteBlog();
        favouriteBlog.setEmail("john.doe@example.com");
        favouriteBlog.setFavoriteBlogs(new ArrayList<>());  // Initialize the favorite blogs set as empty

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(favouriteBlogRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(favouriteBlog));

        assertThrows(FavouriteBlogNotFoundException.class, () -> blogService.removeFavoriteBlog("john.doe@example.com", 1));
    }

    @Test
    void testGetAll() {
        when(mockBlogs.size()).thenReturn(4);
        when(blogService.getAll()).thenReturn(blogs);
        List<Blog> blogs = blogService.getAll();
        assertEquals(4, blogs.size());
    }

    @Test
    void testFilterBlogsByQuery() {
        when(blogService.getAll()).thenReturn(blogs);

        List<Blog> filteredBlogs = blogService.filterBlogs(null, null, "description 2");
        assertEquals(1, filteredBlogs.size());
        assertEquals("Blog 2", filteredBlogs.get(0).getTitle());
    }

    @Test
    void testFilterBlogsByCategory() {
        when(blogService.getAll()).thenReturn(blogs);

        List<Blog> filteredBlogs = blogService.filterBlogs("Category 2", null, null);
        assertEquals(2, filteredBlogs.size());
        assertEquals("Blog 2", filteredBlogs.get(0).getTitle());
        assertEquals("Blog 3", filteredBlogs.get(1).getTitle());
    }

    @Test
    void testFilterBlogsByCountry() {
        when(blogService.getAll()).thenReturn(blogs);

        List<Blog> filteredBlogs = blogService.filterBlogs(null, "Country 2", null);
        assertEquals(2, filteredBlogs.size());
        assertEquals("Blog 2", filteredBlogs.get(0).getTitle());
        assertEquals("Blog 4", filteredBlogs.get(1).getTitle());
    }

    @Test
    void testModifyBlogStatus_Success() throws InvalidBlogStatusException, BlogNotFoundException {
        String adminId = "admin@example.com";
        Integer blogId = 1;
        String status = "APPROVED";
        String normalizedStatus = "APPROVED";

        when(blogRepository.findStatusByBlogId(blogId)).thenReturn(Optional.of("PENDING"));

        doNothing().when(blogRepository).updateStatusByBlogId(blogId, normalizedStatus);

        MessageResponse response = blogService.modifyBlogStatus(adminId, blogId, status);

        assertEquals("Blog status has been successfully set to APPROVED", response.getMessage());
        verify(blogRepository, times(1)).findStatusByBlogId(blogId);
        verify(blogRepository, times(1)).updateStatusByBlogId(blogId, normalizedStatus);
    }

    @Test
    void testModifyBlogStatus_InvalidStatus() {
        String adminId = "admin@example.com";
        Integer blogId = 1;
        String invalidStatus = "INVALID";

        InvalidBlogStatusException exception = assertThrows(
                InvalidBlogStatusException.class,
                () -> blogService.modifyBlogStatus(adminId, blogId, invalidStatus)
        );

        assertEquals("Invalid status provided: INVALID", exception.getMessage());
        verifyNoInteractions(blogRepository);
    }

    @Test
    void testModifyBlogStatus_BlogNotFound() {
        String adminId = "admin@example.com";
        Integer blogId = 1;
        String status = "APPROVED";

        when(blogRepository.findStatusByBlogId(blogId)).thenReturn(Optional.empty());

        BlogNotFoundException exception = assertThrows(
                BlogNotFoundException.class,
                () -> blogService.modifyBlogStatus(adminId, blogId, status)
        );

        assertEquals("Blog not found with ID: 1", exception.getMessage());
        verify(blogRepository, times(1)).findStatusByBlogId(blogId);
        verify(blogRepository, never()).updateStatusByBlogId(anyInt(), anyString());
    }

    @Test
    void testModifyBlogStatus_AlreadyInDesiredState() throws InvalidBlogStatusException, BlogNotFoundException {
        String adminId = "admin@example.com";
        Integer blogId = 1;
        String status = "APPROVED";

        when(blogRepository.findStatusByBlogId(blogId)).thenReturn(Optional.of("APPROVED"));

        MessageResponse response = blogService.modifyBlogStatus(adminId, blogId, status);

        assertEquals("Blog is already in the APPROVED state", response.getMessage());
        verify(blogRepository, times(1)).findStatusByBlogId(blogId);
        verify(blogRepository, never()).updateStatusByBlogId(anyInt(), anyString());
    }

    @Test
    public void testAddComment_BlogFound() {
        Blog blog = new Blog();
        blog.setId(1);
        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setText("Test Comment");

        MessageResponse response = blogService.addComment("test@example.com", commentRequest, 1);

        assertNotNull(response);
        assertEquals("Comment added", response.getMessage());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void testAddComment_BlogNotFound() {
        when(blogRepository.findById(1)).thenReturn(Optional.empty());

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setText("Test Comment");

        Exception exception = assertThrows(BlogNotFoundException.class, () -> {
            blogService.addComment("test@example.com", commentRequest, 1);
        });

        assertEquals("Blog not found with id: 1", exception.getMessage());
    }

    @Test
    public void testMapToCommentResponse() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Nice blog post!");
        comment.setCreationTime(LocalDateTime.now());
        comment.setEmail("user@example.com");

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        when(userClient.getUserByEmail("user@example.com")).thenReturn(user);

        CommentResponse response = blogService.mapToCommentResponse(comment);

        assertNotNull(response);
        assertEquals("John Doe", response.getAuthor());
        assertEquals("Nice blog post!", response.getText());
    }

    @Test
    public void testViewComments_BlogFound() {
        Blog blog = new Blog();
        blog.setId(1);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test Comment");
        comment.setCreationTime(LocalDateTime.now());
        comment.setEmail("test@example.com");
        blog.setComments(Arrays.asList(comment));

        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(userClient.getUserByEmail("test@example.com")).thenReturn(user);

        List<CommentResponse> responses = blogService.viewComments(1, 0, 10);

        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("Test Comment", responses.get(0).getText());
        assertEquals("Test User", responses.get(0).getAuthor());
    }

    @Test
    public void testViewComments_BlogNotFound() {
        when(blogRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(BlogNotFoundException.class, () -> {
            blogService.viewComments(1, 0, 10);
        });

        assertEquals("Blog not found with id: 1", exception.getMessage());
    }
    @Test
    void getAllFavouriteBlogs_Success() {
        // Setup
        Blog blog1 = new Blog();
        blog1.setId(1);
        blog1.setTitle("Blog 1");
        blog1.setEmail("user@example.com");

        Blog blog2 = new Blog();
        blog2.setId(2);
        blog2.setTitle("Blog 2");
        blog2.setEmail("user@example.com");

        FavouriteBlog favouriteBlog = new FavouriteBlog();
        favouriteBlog.setEmail("user@example.com");
        favouriteBlog.setFavoriteBlogs(Arrays.asList(blog1, blog2));

        UserProfileResponse userProfile = new UserProfileResponse();
        userProfile.setFirstName("John");
        userProfile.setLastName("Doe");
        userProfile.setImageURL("sample-url");

        when(favouriteBlogRepository.findByEmail("user@example.com")).thenReturn(Optional.of(favouriteBlog));
        when(userClient.getUserProfileByEmail("user@example.com")).thenReturn(userProfile);

        List<BlogSummaryResponse> results = blogService.getAllFavouriteBlogs("user@example.com");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Blog 1", results.get(0).getTitle());
        assertEquals("Blog 2", results.get(1).getTitle());
        verify(favouriteBlogRepository).findByEmail("user@example.com");
        verify(userClient, times(2)).getUserProfileByEmail("user@example.com");  // Called twice, once for each blog
    }


    @Test
    public void testGetMimeType_GIF() {
        // GIF file signature
        byte[] gifData = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38};
        String mimeType = BlogServiceImpl.getMimeType(gifData);
        assertEquals("image/gif", mimeType);
    }

    @Test
    public void testGetMimeType_Unknown() {
        // Random data, not a known image format
        byte[] unknownData = new byte[] {0x01, 0x02, 0x03, 0x04};
        String mimeType = BlogServiceImpl.getMimeType(unknownData);
        assertNull(mimeType);
    }

    @Test
    public void testGetMimeType_EmptyData() {
        // Empty data
        byte[] emptyData = new byte[] {};
        String mimeType = BlogServiceImpl.getMimeType(emptyData);
        assertNull(mimeType);
    }

    @Test
    public void testGetMimeType_NullData() {
        // Null data
        Exception exception = assertThrows(NullPointerException.class, () -> {
            BlogServiceImpl.getMimeType(null);
        });

        assertEquals("Cannot read the array length because \"buf\" is null", exception.getMessage());}

    @Test
    void getAllFavouriteBlogs_NotFound() {
        when(favouriteBlogRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(FavouriteBlogNotFoundException.class, () -> blogService.getAllFavouriteBlogs("user@example.com"));
    }


    @Test
    void checkFavorites_WithExistingFavouriteBlog_ReturnsBlogSummaryResponsesWithFavorites() {
        // Arrange
        String email = "test@example.com";
        List<BlogSummaryResponse> blogSummaryResponses = Arrays.asList(new BlogSummaryResponse(), new BlogSummaryResponse());
        FavouriteBlog favouriteBlog = new FavouriteBlog();
        when(favouriteBlogRepository.findByEmail(email)).thenReturn(Optional.of(favouriteBlog));

        // Act
        List<BlogSummaryResponse> result = blogService.checkFavorites(email, blogSummaryResponses);

        // Assert
        assertEquals(blogSummaryResponses.size(), result.size());
        verify(favouriteBlogRepository, times(1)).findByEmail(email);
    }

    @Test
    void checkFavorites_WithNonExistingFavouriteBlog_ThrowsFavouriteBlogNotFoundException() {
        // Arrange
        String email = "test@example.com";
        List<BlogSummaryResponse> blogSummaryResponses = Arrays.asList(new BlogSummaryResponse(), new BlogSummaryResponse());
        when(favouriteBlogRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(FavouriteBlogNotFoundException.class, () -> blogService.checkFavorites(email, blogSummaryResponses));
        verify(favouriteBlogRepository, times(1)).findByEmail(email);
    }

    @Test
    void setIsFavorite_WithFavoriteBlog_SetsIsFavoriteToOne() {
        // Arrange
        BlogSummaryResponse blogSummaryResponse = new BlogSummaryResponse();
        FavouriteBlog favouriteBlog = new FavouriteBlog();
        Blog blog = new Blog();
        blog.setId(1);
        blogSummaryResponse.setId(1);
        favouriteBlog.setFavoriteBlogs(List.of(blog));
        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));

        // Act
        BlogSummaryResponse result = blogService.setIsFavorite(blogSummaryResponse, favouriteBlog);

        // Assert
        assertEquals(1, result.getIsFavorite());
    }

    @Test
    void setIsFavorite_WithNonFavoriteBlog_SetsIsFavoriteToZero() {
        // Arrange
        BlogSummaryResponse blogSummaryResponse = new BlogSummaryResponse();
        FavouriteBlog favouriteBlog = new FavouriteBlog();
        blogSummaryResponse.setId(1);
        favouriteBlog.setFavoriteBlogs(Collections.emptyList());

        // Act
        BlogSummaryResponse result = blogService.setIsFavorite(blogSummaryResponse, favouriteBlog);

        // Assert
        assertEquals(0, result.getIsFavorite());
    }

    @Test
    void isFavorite_WithFavoriteBlog_ReturnsTrue() {
        // Arrange
        BlogSummaryResponse blogSummaryResponse = new BlogSummaryResponse();
        FavouriteBlog favouriteBlog = new FavouriteBlog();
        Blog blog = new Blog();
        blog.setId(1);
        blogSummaryResponse.setId(1);
        favouriteBlog.setFavoriteBlogs(List.of(blog));
        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));

        // Act
        boolean result = blogService.isFavorite(blogSummaryResponse, favouriteBlog);

        // Assert
        assertTrue(result);
    }

    @Test
    void isFavorite_WithNonFavoriteBlog_ReturnsFalse() {
        // Arrange
        BlogSummaryResponse blogSummaryResponse = new BlogSummaryResponse();
        FavouriteBlog favouriteBlog = new FavouriteBlog();
        blogSummaryResponse.setId(1);
        favouriteBlog.setFavoriteBlogs(Collections.emptyList());
        // Act
        boolean result = blogService.isFavorite(blogSummaryResponse, favouriteBlog);

        // Assert
        assertFalse(result);
    }


    @Test
    void reportComment_ValidComment_Success() {
        // Arrange
        String email = "test@example.com";
        int commentId = 1;
        Comment comment = new Comment();
        comment.setId((long) commentId);
        when(commentRepository.findById((long) commentId)).thenReturn(Optional.of(comment));
        when(reportRepository.findByReportedByAndCommentID(email, commentId)).thenReturn(new ArrayList<>());

        // Act
        MessageResponse response = blogService.reportComment(email, commentId);

        // Assert
        assertEquals("Comment Reported", response.getMessage());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void reportComment_CommentNotFound_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        int commentId = 1;
        when(commentRepository.findById((long) commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CommentNotFound.class, () -> blogService.reportComment(email, commentId));
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void reportComment_DuplicateReport_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        int commentId = 1;
        Comment comment = new Comment();
        comment.setId((long) commentId);
        when(commentRepository.findById((long) commentId)).thenReturn(Optional.of(comment));
        List<Report> existingReports = new ArrayList<>();
        existingReports.add(new Report());
        when(reportRepository.findByReportedByAndCommentID(email, commentId)).thenReturn(existingReports);

        // Act & Assert
        DuplicateReportException exception = assertThrows(DuplicateReportException.class, () -> blogService.reportComment(email, commentId));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("User already reported this comment", exception.getMessage());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void isReportDuplicate_DuplicateExists_ReturnsTrue() {
        // Arrange
        String reportedBy = "test@example.com";
        int commentID = 1;
        List<Report> existingReports = new ArrayList<>();
        existingReports.add(new Report());
        when(reportRepository.findByReportedByAndCommentID(reportedBy, commentID)).thenReturn(existingReports);

        // Act
        boolean isDuplicate = blogService.isReportDuplicate(reportedBy, commentID);

        // Assert
        assertTrue(isDuplicate);
    }

    @Test
    void isReportDuplicate_NoDuplicate_ReturnsFalse() {
        // Arrange
        String reportedBy = "test@example.com";
        int commentID = 1;
        when(reportRepository.findByReportedByAndCommentID(reportedBy, commentID)).thenReturn(new ArrayList<>());

        // Act
        boolean isDuplicate = blogService.isReportDuplicate(reportedBy, commentID);

        // Assert
        assertFalse(isDuplicate);
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
    void getApprovedBlogs_Found() {
        // Arrange
        Blog blog1 = createBlog(1, "Approved Blog 1", "APPROVED", "author1@example.com");
        Blog blog2 = createBlog(2, "Approved Blog 2", "APPROVED", "author2@example.com");
        when(blogRepository.findBlogsByStatus("APPROVED")).thenReturn(Optional.of(Arrays.asList(blog1, blog2)));

        // Act
        List<Blog> result = blogService.getApprovedBlogs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Approved Blog 1", result.get(0).getTitle());
        verify(blogRepository, times(1)).findBlogsByStatus("APPROVED");
    }

    @Test
    void getApprovedBlogs_NotFound() {
        // Arrange
        when(blogRepository.findBlogsByStatus("APPROVED")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BlogNotFoundException.class, () -> blogService.getApprovedBlogs());
        verify(blogRepository, times(1)).findBlogsByStatus("APPROVED");
    }

    @Test
    void getRejectedBlogs_Found() {
        // Arrange
        Blog blog1 = createBlog(3, "Rejected Blog 1", "REJECTED", "author3@example.com");
        when(blogRepository.findBlogsByStatus("REJECTED")).thenReturn(Optional.of(Arrays.asList(blog1)));

        // Act
        List<Blog> result = blogService.getRejectedBlogs();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Rejected Blog 1", result.get(0).getTitle());
        verify(blogRepository, times(1)).findBlogsByStatus("REJECTED");
    }

    @Test
    void getRejectedBlogs_NotFound() {
        // Arrange
        when(blogRepository.findBlogsByStatus("REJECTED")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BlogNotFoundException.class, () -> blogService.getRejectedBlogs());
        verify(blogRepository, times(1)).findBlogsByStatus("REJECTED");
    }

    @Test
    void getPendingBlogs_Found() {
        // Arrange
        Blog blog1 = createBlog(4, "Pending Blog 1", "PENDING", "author4@example.com");
        Blog blog2 = createBlog(5, "Pending Blog 2", "PENDING", "author5@example.com");
        when(blogRepository.findBlogsByStatus("PENDING")).thenReturn(Optional.of(Arrays.asList(blog1, blog2)));

        // Act
        List<Blog> result = blogService.getPendingBlogs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Pending Blog 1", result.get(0).getTitle());
        verify(blogRepository, times(1)).findBlogsByStatus("PENDING");
    }

    @Test
    void getPendingBlogs_NotFound() {
        // Arrange
        when(blogRepository.findBlogsByStatus("PENDING")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BlogNotFoundException.class, () -> blogService.getPendingBlogs());
        verify(blogRepository, times(1)).findBlogsByStatus("PENDING");
    }

}