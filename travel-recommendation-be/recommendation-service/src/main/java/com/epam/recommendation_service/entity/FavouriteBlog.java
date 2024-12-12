package com.epam.recommendation_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Table(name = "favorites")
public class FavouriteBlog {
    @Id
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY) // Adjusted cascade and fetch type
    private List<Blog> favoriteBlogs=new ArrayList<>();

    public void addFavoriteBlog(Blog blog) {
        if (this.favoriteBlogs == null) {
            this.favoriteBlogs = new ArrayList<>();
        }
        this.favoriteBlogs.add(blog);
    }

    public void removeFavoriteBlog(Blog blog) {
        this.favoriteBlogs.remove(blog);
    }
    }
