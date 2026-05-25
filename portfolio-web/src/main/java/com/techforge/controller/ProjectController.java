package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Project;
import com.techforge.entity.Project.ProjectStatus;
import com.techforge.entity.ProjectSkill;
import com.techforge.entity.Skill;
import com.techforge.repository.ProjectRepository;
import com.techforge.repository.ProjectSkillRepository;
import com.techforge.repository.SkillRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目接口
 */
@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final SkillRepository skillRepository;

    public ProjectController(ProjectRepository projectRepository,
                        ProjectSkillRepository projectSkillRepository,
                        SkillRepository skillRepository) {
        this.projectRepository = projectRepository;
        this.projectSkillRepository = projectSkillRepository;
        this.skillRepository = skillRepository;
    }

    /**
     * 项目列表 (公开)
     */
    @GetMapping("/projects")
    public Result<List<Project>> getProjects() {
        List<Project> projects = projectRepository.findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(ProjectStatus.PUBLISHED);
        // 加载每个项目的关联技能
        for (Project p : projects) {
            p.setSkills(loadSkillsForProject(p.getId()));
        }
        return Result.success(projects);
    }

    /**
     * 项目详情 (公开)
     */
    @GetMapping("/projects/{id}")
    public Result<Project> getProject(@PathVariable Integer id) {
        return projectRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .map(p -> {
                    p.setSkills(loadSkillsForProject(p.getId()));
                    return Result.success(p);
                })
                .orElse(Result.error(404, "项目不存在"));
    }

    private List<Skill> loadSkillsForProject(Integer projectId) {
        List<ProjectSkill> pss = projectSkillRepository.findByProjectId(projectId);
        if (pss.isEmpty()) return new ArrayList<>();
        List<Integer> skillIds = pss.stream().map(ProjectSkill::getSkillId).collect(Collectors.toList());
        return skillRepository.findAllById(skillIds);
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
        project.setProjectType(request.getProjectType());
        project.setFeatures(request.getFeatures());
        project.setStructure(request.getStructure());
        project.setSummary(request.getSummary());

        projectRepository.save(project);

        // 保存关联技能
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            saveProjectSkills(project.getId(), request.getSkillIds());
        }

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
                    if (request.getProjectType() != null) project.setProjectType(request.getProjectType());
                    if (request.getFeatures() != null) project.setFeatures(request.getFeatures());
                    if (request.getStructure() != null) project.setStructure(request.getStructure());
                    if (request.getSummary() != null) project.setSummary(request.getSummary());

                    projectRepository.save(project);

                    // 更新关联技能
                    if (request.getSkillIds() != null) {
                        updateProjectSkills(id, request.getSkillIds());
                    }

                    return Result.success("项目更新成功");
                })
                .orElse(Result.error(404, "项目不存在"));
    }

    private void saveProjectSkills(Integer projectId, List<Integer> skillIds) {
        for (Integer skillId : skillIds) {
            ProjectSkill ps = new ProjectSkill();
            ps.setProjectId(projectId);
            ps.setSkillId(skillId);
            projectSkillRepository.save(ps);
        }
    }

    private void updateProjectSkills(Integer projectId, List<Integer> newSkillIds) {
        // 删除旧的
        projectSkillRepository.deleteByProjectId(projectId);
        // 添加新的
        if (newSkillIds != null && !newSkillIds.isEmpty()) {
            saveProjectSkills(projectId, newSkillIds);
        }
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
        private String projectType;
        private String features;
        private String structure;
        private String summary;
        private List<Integer> skillIds;

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
        public String getProjectType() { return projectType; }
        public void setProjectType(String projectType) { this.projectType = projectType; }
        public String getFeatures() { return features; }
        public void setFeatures(String features) { this.features = features; }
        public String getStructure() { return structure; }
        public void setStructure(String structure) { this.structure = structure; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public List<Integer> getSkillIds() { return skillIds; }
        public void setSkillIds(List<Integer> skillIds) { this.skillIds = skillIds; }
    }
}