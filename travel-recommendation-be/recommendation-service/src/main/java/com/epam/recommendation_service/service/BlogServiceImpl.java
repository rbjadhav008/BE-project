package com.epam.recommendation_service.service;

import com.epam.recommendation_service.client.UserClient;
import com.epam.recommendation_service.dto.*;
import com.epam.recommendation_service.entity.*;
import com.epam.recommendation_service.exception.*;
import com.epam.recommendation_service.repository.BlogRepository;
import com.epam.recommendation_service.repository.CommentRepository;
import com.epam.recommendation_service.repository.FavouriteBlogRepository;
import com.epam.recommendation_service.repository.ReportRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService{

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final FavouriteBlogRepository favouriteBlogRepository;
    private static final byte[] DEFAULT_IMAGE;

    private final UserClient userClient;

    static {
        try {
            ClassPathResource resource = new ClassPathResource("static/default-image.jpg");
            DEFAULT_IMAGE = StreamUtils.copyToByteArray(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default image", e);
        }
    }

        @Override
        public BlogResponse createBlog(BlogRequest blogRequest, String userEmail) throws IOException {

            User user = userClient.getUserByEmail(userEmail);
            Blog blog = new Blog();
            blog.setAuthorName(user.getFirstName()+" "+user.getLastName());
            blog.setTitle(blogRequest.getTitle());
            blog.setDescription(blogRequest.getDescription());
            blog.setCategory(blogRequest.getCategory());
            blog.setCity(blogRequest.getCity());
            blog.setCountry(blogRequest.getCountry());
            blog.setEmail(userEmail);
            blog.setStatus("PENDING");

            BlogResponse blogResponse = new BlogResponse();

            if (blogRequest.getImage() != null && !blogRequest.getImage().isEmpty()) {
                try {
                    blog.setImage(blogRequest.getImage().getBytes());
                } catch (IOException e) {
                    throw new IOException(e);
                }
            } else {
                blog.setImage(DEFAULT_IMAGE);
                blogResponse.setImageURL("You haven't uploaded an image");
            }

            blog.setCreationTime(LocalDateTime.now());

            Blog savedBlog = blogRepository.save(blog);
            blogResponse.setId(savedBlog.getId());
            blogResponse.setTitle(savedBlog.getTitle());
            blogResponse.setDescription(savedBlog.getDescription());
            blogResponse.setCategory(savedBlog.getCategory());
            blogResponse.setCity(savedBlog.getCity());
            blogResponse.setCountry(savedBlog.getCountry());

           if(blogResponse.getImageURL()==null) {
               blogResponse.setImageURL("Image Uploaded");
           }


            return blogResponse;
        }

    @Override
    public BlogSummaryResponse getBlogById(Integer blogId) throws BlogNotFoundException {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        return mapToBlogSummaryDTO(blog);

    }

    @Override
    public Page<BlogSummaryResponse> getBlogsByPage(int pageNo, int pageSize) {
        Page<Blog> blogPage =
                blogRepository.findByStatus("APPROVED", PageRequest.of(pageNo,pageSize));
        List<BlogSummaryResponse> blogSummaryResponses = blogPage.getContent().stream()
                .map(this::mapToBlogSummaryDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(blogSummaryResponses,blogPage.getPageable(),blogPage.getTotalElements());
    }

    @Override
    public List<BlogSummaryResponse> getAllBlogs(String email) {
        List<Blog> blogPage = blogRepository.findAcceptedBlogs();

        List<BlogSummaryResponse> blogSummaryResponses = blogPage.stream()
                .map(this::mapToBlogSummaryDTO)
                .collect(Collectors.toList());

        if(!email.equals("GUEST")){
             return checkFavorites(email,blogSummaryResponses);
        }

        return blogSummaryResponses;

    }

    public List<BlogSummaryResponse> checkFavorites(String email, List<BlogSummaryResponse> blogSummaryResponses){
        FavouriteBlog favouriteBlog = favouriteBlogRepository.findByEmail(email).orElseThrow(()-> new FavouriteBlogNotFoundException("Favourite blog not found for email: " + email));
        return blogSummaryResponses.stream().map(blogSummaryResponse ->
            setIsFavorite(blogSummaryResponse, favouriteBlog))
                .collect(Collectors.toList());
    }

    BlogSummaryResponse setIsFavorite(BlogSummaryResponse blogSummaryResponse, FavouriteBlog favouriteBlog) {
        if(isFavorite(blogSummaryResponse, favouriteBlog)){
            blogSummaryResponse.setIsFavorite(1);
        } else{
            blogSummaryResponse.setIsFavorite(0);
        }
        return blogSummaryResponse;
    }

    boolean isFavorite(BlogSummaryResponse blogSummaryResponse, FavouriteBlog favouriteBlog) {
        return favouriteBlog.getFavoriteBlogs()
                .stream()
                .anyMatch(blog -> Integer.valueOf(blog.getId()).equals(blogSummaryResponse.getId()));
    }

    @Override
    public MessageResponse deleteBlog(int blogId) {
        Blog blog = blogRepository.findById(blogId).
                orElseThrow(()-> new BlogNotFoundException("Blog not found with id: "+blogId));
        blog.setStatus("REJECTED");
        blogRepository.save(blog);
        return new MessageResponse("Blog deleted");
    }

    @Override
    public Blog updateBlog(int id, BlogRequest blogRequest, String email) throws BlogNotFoundException, IOException, UnauthorizedBlogAccessException {
        Blog existingBlog = blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + id));

        if (!existingBlog.getEmail().equals(email)) {
            throw new UnauthorizedBlogAccessException("You are not authorized to update this blog.");
        }


        existingBlog.setTitle(blogRequest.getTitle());
        existingBlog.setDescription(blogRequest.getDescription());
        existingBlog.setCategory(blogRequest.getCategory());
        existingBlog.setCity(blogRequest.getCity());
        existingBlog.setCountry(blogRequest.getCountry());
        existingBlog.setStatus("PENDING");

        if(blogRequest.getImage() != null && !blogRequest.getImage().isEmpty()) {
            existingBlog.setImage(blogRequest.getImage().getBytes());
        }


        return blogRepository.save(existingBlog);
    }

    @Override
    public String addFavorite(String email, int blogId) throws UnauthorizedBlogAccessException {

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with ID: " + blogId));

        FavouriteBlog user = favouriteBlogRepository.findByEmail(email)
                .orElseGet(() -> {
                    FavouriteBlog newUser = new FavouriteBlog();
                    newUser.setEmail(email);
                    return newUser;
                });
        if(blog.getStatus().equals("REJECTED")){
            throw new UnauthorizedBlogAccessException("Cannot add rejected blog to favorite");
        }
        if(user.getFavoriteBlogs().contains(blog)) {
            throw new BlogAlreadyPresentException("Blog already favorited by User with ID: " + email);
        }


        user.addFavoriteBlog(blog);
        favouriteBlogRepository.save(user);

        return "favorite blog added successfully";
    }


    @Override
    public String removeFavoriteBlog(String email, int blogId) {

        FavouriteBlog user = favouriteBlogRepository.findByEmail(email).orElseGet(() -> {
            FavouriteBlog newUser = new FavouriteBlog();
            newUser.setEmail(email);
            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new BlogNotFoundException("Blog not found"));
            newUser.addFavoriteBlog(blog);
            favouriteBlogRepository.save(newUser);
            return newUser;
        });

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found"));

        if (!user.getFavoriteBlogs().contains(blog)) {
            throw new FavouriteBlogNotFoundException("This blog is not added as a favorite");
        }

        user.removeFavoriteBlog(blog);
        favouriteBlogRepository.save(user);

        return "Favorite blog removed successfully";
    }
    BlogSummaryResponse mapToBlogSummaryDTO(Blog blog) {
        BlogSummaryResponse blogSummaryResponse = new BlogSummaryResponse();

        blogSummaryResponse.setId(blog.getId());
        blogSummaryResponse.setTitle(blog.getTitle());
        blogSummaryResponse.setDescription(blog.getDescription());
        blogSummaryResponse.setImageURL(getImageURL(blog));
        blogSummaryResponse.setCity(blog.getCity());
        blogSummaryResponse.setCountry(blog.getCountry());
        blogSummaryResponse.setCategory(blog.getCategory());
        blogSummaryResponse.setCreationTime(blog.getCreationTime());
        blogSummaryResponse.setStatus(blog.getStatus());

        UserProfileResponse userProfileResponse = getBlogAuthorDetails(blog);
        blogSummaryResponse.setAuthor(userProfileResponse.getFirstName()+" "
                +userProfileResponse.getLastName());
        blogSummaryResponse.setAuthorImageURL(userProfileResponse.getImageURL());
        blogSummaryResponse.setIsFavorite(0);
        return blogSummaryResponse;
    }


    @Override
    public List<Blog> getAll() {
        return blogRepository.findAll();
    }

    @Override
    public List<Blog> filterBlogs(String category, String country, String query) {
        List<Blog> filteredBlogs = getAll();

        if (query != null) {
            filteredBlogs = filteredBlogs.stream()
                    .filter(blog -> blog.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                            blog.getDescription().toLowerCase().contains(query.toLowerCase()))
                    .toList();
        }

        if (category != null && !category.isEmpty()) {
            filteredBlogs = filteredBlogs.stream()
                    .filter(blog -> blog.getCategory() != null && blog.getCategory().equalsIgnoreCase(category))
                    .toList();
        }

        if (country != null && !country.isEmpty()) {
            filteredBlogs = filteredBlogs.stream()
                    .filter(blog -> blog.getCountry() != null && blog.getCountry().equalsIgnoreCase(country))
                    .toList();
        }

        return filteredBlogs;
    }
    public List<BlogSummaryResponse> getMyBlogs(String email) {

            List<Blog> blogs = blogRepository.findAllByEmail(email);
            if (blogs.isEmpty()) {
                throw new BlogNotFoundException("No blogs found for the given user ID: " + email);
            }

            return blogs.stream()
                    .map(blog -> new BlogSummaryResponse(
                            blog.getId(),
                            blog.getTitle(),getImageURL(blog) ,
                            blog.getDescription(),
                            blog.getCity(),
                            blog.getCountry(),
                            blog.getCategory(),
                            blog.getAuthorName(),
                            " ",
                            blog.getStatus(),
                            blog.getCreationTime(),
                            0
                    ))
                    .collect(Collectors.toList());

    }

    public UserProfileResponse getBlogAuthorDetails(Blog blog) {
        String email = blog.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return userClient.getUserProfileByEmail(email);
    }

    @Override
    public List<BlogSummaryResponse> getAllFavouriteBlogs(String email) {
        Optional<FavouriteBlog> favouriteBlogsOpt = favouriteBlogRepository.findByEmail(email);

        if (!favouriteBlogsOpt.isPresent() || favouriteBlogsOpt.get().getFavoriteBlogs().isEmpty()) {
            throw new FavouriteBlogNotFoundException("No favourite blogs found for the given user ID: " + email);
        }

        List<Blog> favouriteBlogs = favouriteBlogsOpt.get().getFavoriteBlogs();
        System.out.println(favouriteBlogs);
        return favouriteBlogs.stream()
                .map(this::mapToBlogSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse addComment(String email, CommentRequest commentRequest, int blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setCreationTime(LocalDateTime.now());
        comment.setBlog(blog);
        comment.setEmail(email);

        commentRepository.save(comment);
        return new MessageResponse("Comment added");
    }

    @Override
    public List<CommentResponse> viewComments(int blogId, int pageNo, int pageSize) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));
        List<Comment> comments = blog.getComments();
        List<CommentResponse> commentResponses = comments.stream()
                .map(this::mapToCommentResponse).collect(Collectors.toList());

        return commentResponses;
    }

    @Override
    public MessageResponse reportComment(String email, int commentId) {
        Comment comment = commentRepository.findById((long) commentId)
                .orElseThrow(() -> new CommentNotFound("Comment not found with id: " + commentId));
        if(isReportDuplicate(email,commentId)){
            throw new DuplicateReportException(HttpStatus.BAD_REQUEST,"User already reported this comment");
        }
        Report report = new Report();
        report.setCommentID(commentId);
        report.setReportedBy(email);
        reportRepository.save(report);
        return new MessageResponse("Comment Reported");
    }

    public boolean isReportDuplicate(String reportedBy, int commentID) {
        List<Report> existingReports = reportRepository.findByReportedByAndCommentID(reportedBy, commentID);
        return !existingReports.isEmpty();
    }

    //    private static PageImpl<CommentResponse> getCommentResponses(int pageNo, int pageSize, List<CommentResponse> commentResponses) {
//        Pageable pageable = PageRequest.of(pageNo, pageSize);
//        int start = (int) pageable.getOffset();
//        int end = Math.min((start + pageable.getPageSize()), commentResponses.size());
//        List<CommentResponse> pageContent = commentResponses.subList(start, end);
//
//        return new PageImpl<>(pageContent, pageable, commentResponses.size());
//    }

    CommentResponse mapToCommentResponse(Comment comment){
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(comment.getId());
        commentResponse.setText(comment.getText());
        commentResponse.setCreationTime(comment.getCreationTime());

        User user = userClient.getUserByEmail(comment.getEmail());
        String author = user.getFirstName() + " " + user.getLastName();

        commentResponse.setAuthor(author);

        return commentResponse;
    }

    public MessageResponse modifyBlogStatus(String adminId, Integer blogId, String status) throws InvalidBlogStatusException, BlogNotFoundException {

        String normalizedStatus = status.trim().toUpperCase();

        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(normalizedStatus)) {
            throw new InvalidBlogStatusException("Invalid status provided: " + status);
        }

        String currentStatus = blogRepository.findStatusByBlogId(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with ID: " + blogId));


        if (currentStatus.equals(normalizedStatus)) {
            return new MessageResponse("Blog is already in the " + normalizedStatus + " state");
        }

        blogRepository.updateStatusByBlogId(blogId, normalizedStatus);

        return new MessageResponse("Blog status has been successfully set to " + normalizedStatus);
    }


    private static String getImageURL(Blog blog) {
        if(blog.getImage() == null){
            return "https://dynamic-media-cdn.tripadvisor.com/" +
                    "media/photo-o/12/59/82/3a/beach-attached-to-the.jpg?w=1000&h=-1&s=1";
        } else {
            return "data:" + getMimeType(blog.getImage()) + ";base64," + Base64.getEncoder().encodeToString(blog.getImage());
        }
    }

    public static String getMimeType(byte[] imageData) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(imageData)) {
            return URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            System.err.println("Error determining MIME type: " + e.getMessage());
            return "application/octet-stream";
        }
    }


    @Override
    public List<Blog> getApprovedBlogs() {
        return blogRepository.findBlogsByStatus("APPROVED").orElseThrow(() ->
                new BlogNotFoundException("No approved blogs found."));
    }



    @Override
    public List<Blog> getRejectedBlogs() {
        return blogRepository.findBlogsByStatus("REJECTED").orElseThrow(() ->
                new BlogNotFoundException("No rejected blogs found."));
    }


    @Override
    public List<Blog> getPendingBlogs() {
        return blogRepository.findBlogsByStatus("PENDING").orElseThrow(() ->
                new BlogNotFoundException("No pending blogs found."));
    }


}

