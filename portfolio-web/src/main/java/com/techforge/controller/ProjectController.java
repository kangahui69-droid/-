package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Project;
import com.techforge.entity.Project.ProjectStatus;
import com.techforge.repository.ProjectRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目接口
 */
@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * 项目列表 (公开)
     */
    @GetMapping("/projects")
    public Result<List<Project>> getProjects() {
        List<Project> projects = projectRepository.findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(ProjectStatus.PUBLISHED);
        return Result.success(projects);
    }

    /**
     * 项目详情 (公开)
     */
    @GetMapping("/projects/{id}")
    public Result<Project> getProject(@PathVariable Integer id) {
        return projectRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .map(Result::success)
                .orElse(Result.error(404, "项目不存在"));
    }

    // ========== 管理接口 ==========

    @PostMapping("/admin/projects")
    @PreAuthorize("isAuthenticated()")
    public Result<?> createProject(@RequestBody ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setCoverImage(request.getCoverImage());
        project.setDemoUrl(request.getDemoUrl());
        project.setGithubUrl(request.getGithubUrl());
        project.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.DRAFT);

        projectRepository.save(project);
        return Result.success("项目创建成功");
    }

    @PutMapping("/admin/projects/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updateProject(@PathVariable Integer id, @RequestBody ProjectRequest request) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (request.getName() != null) project.setName(request.getName());
                    if (request.getDescription() != null) project.setDescription(request.getDescription());
                    if (request.getTechStack() != null) project.setTechStack(request.getTechStack());
                    if (request.getCoverImage() != null) project.setCoverImage(request.getCoverImage());
                    if (request.getDemoUrl() != null) project.setDemoUrl(request.getDemoUrl());
                    if (request.getGithubUrl() != null) project.setGithubUrl(request.getGithubUrl());
                    if (request.getSortOrder() != null) project.setSortOrder(request.getSortOrder());
                    if (request.getStatus() != null) project.setStatus(request.getStatus());

                    projectRepository.save(project);
                    return Result.success("项目更新成功");
                })
                .orElse(Result.error(404, "项目不存在"));
    }

    @DeleteMapping("/admin/projects/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteProject(@PathVariable Integer id) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setDeletedAt(LocalDateTime.now());
                    projectRepository.save(project);
                    return Result.success("项目删除成功");
                })
                .orElse(Result.error(404, "项目不存在"));
    }

    public static class ProjectRequest {
        private String name;
        private String description;
        private String techStack;
        private String coverImage;
        private String demoUrl;
        private String githubUrl;
        private Integer sortOrder;
        private ProjectStatus status;

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
    }
}