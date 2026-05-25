package com.techforge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目展示
 */
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "tech_stack", length = 300)
    private String techStack;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "demo_url")
    private String demoUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "project_type", length = 50)
    private String projectType;

    @Column(columnDefinition = "JSON")
    private String features;

    @Column(columnDefinition = "TEXT")
    private String structure;

    @Column(columnDefinition = "TEXT")
    private String summary;

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // 关联技能（不映射，由 Service 层自行处理）
    private transient java.util.List<Skill> skills;

    public java.util.List<Skill> getSkills() { return skills; }
    public void setSkills(java.util.List<Skill> skills) { this.skills = skills; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    public String getStructure() { return structure; }
    public void setStructure(String structure) { this.structure = structure; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public enum ProjectStatus {
        DRAFT, PUBLISHED
    }
}