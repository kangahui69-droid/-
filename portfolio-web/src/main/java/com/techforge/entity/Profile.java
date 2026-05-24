package com.techforge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 个人简介
 */
@Entity
@Table(name = "profile")
public class Profile {

    @Id
    private Integer id;  // 仅一条数据，id=1

    private String name;
    private String avatar;
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(name = "social_github")
    private String socialGithub;

    @Column(name = "social_email")
    private String socialEmail;

    @Column(name = "social_x")
    private String socialX;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getSocialGithub() { return socialGithub; }
    public void setSocialGithub(String socialGithub) { this.socialGithub = socialGithub; }

    public String getSocialEmail() { return socialEmail; }
    public void setSocialEmail(String socialEmail) { this.socialEmail = socialEmail; }

    public String getSocialX() { return socialX; }
    public void setSocialX(String socialX) { this.socialX = socialX; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}