package com.techforge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目-技能关联表（中间表）
 */
@Entity
@Table(name = "project_skill")
public class ProjectSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "skill_id", nullable = false)
    private Integer skillId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public Integer getSkillId() { return skillId; }
    public void setSkillId(Integer skillId) { this.skillId = skillId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}