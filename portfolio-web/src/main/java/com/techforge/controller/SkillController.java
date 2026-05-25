package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.dto.SkillTreeDTO;
import com.techforge.dto.SkillTreeDTO.Category;
import com.techforge.dto.SkillTreeDTO.SkillVO;
import com.techforge.entity.Skill;
import com.techforge.repository.SkillRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 技能接口
 */
@RestController
@RequestMapping("/api")
public class SkillController {

    private final SkillRepository skillRepository;

    public SkillController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    /**
     * 技能列表 (公开)
     */
    @GetMapping("/skills")
    public Result<List<Skill>> getSkills() {
        List<Skill> skills = skillRepository.findAllByOrderBySortOrderAsc();
        return Result.success(skills);
    }

    /**
     * 技能树 (公开)
     */
    @GetMapping("/skills/tree")
    public Result<SkillTreeDTO> getSkillTree() {
        List<Skill> skills = skillRepository.findAll();

        // 按分类分组
        Map<String, List<Skill>> grouped = skills.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() != null ? s.getCategory() : "other"));

        // 构建分类
        Map<String, String> categoryNames = new LinkedHashMap<>();
        categoryNames.put("frontend", "前端开发");
        categoryNames.put("backend", "后端开发");
        categoryNames.put("ai", "人工智能");
        categoryNames.put("hardware", "硬件/嵌入式的");
        categoryNames.put("other", "其他");

        SkillTreeDTO tree = new SkillTreeDTO();
        List<Category> categories = new ArrayList<>();

        categoryNames.forEach((id, name) -> {
            if (grouped.containsKey(id)) {
                Category cat = new Category();
                cat.setId(id);
                cat.setName(name);
                List<SkillVO> skillVOList = grouped.get(id).stream()
                        .sorted(Comparator.comparingInt(Skill::getSortOrder))
                        .map(s -> {
                            SkillVO vo = new SkillVO();
                            vo.setId(s.getId());
                            vo.setName(s.getName());
                            vo.setLevel(s.getLevel() != null ? s.getLevel() : 1);
                            vo.setIcon(s.getIconUrl());
                            return vo;
                        })
                        .collect(Collectors.toList());
                cat.setSkills(skillVOList);
                categories.add(cat);
            }
        });

        tree.setCategories(categories);
        return Result.success(tree);
    }

    // ========== 管理接口 ==========

    @PostMapping("/admin/skills")
    @PreAuthorize("isAuthenticated()")
    public Result<?> createSkill(@RequestBody SkillRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return Result.error(400, "技能名称不能为空");
        }

        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        skill.setCategory(request.getCategory() != null ? request.getCategory() : "other");
        skill.setLevel(request.getLevel() != null ? request.getLevel() : 1);
        skill.setIconUrl(request.getIconUrl());

        skillRepository.save(skill);
        return Result.success("技能添加成功");
    }

    @PutMapping("/admin/skills/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updateSkill(@PathVariable Integer id, @RequestBody SkillRequest request) {
        return skillRepository.findById(id)
                .map(skill -> {
                    if (request.getName() != null) skill.setName(request.getName());
                    if (request.getSortOrder() != null) skill.setSortOrder(request.getSortOrder());
                    if (request.getCategory() != null) skill.setCategory(request.getCategory());
                    if (request.getLevel() != null) skill.setLevel(request.getLevel());
                    if (request.getIconUrl() != null) skill.setIconUrl(request.getIconUrl());

                    skillRepository.save(skill);
                    return Result.success("技能更新成功");
                })
                .orElse(Result.error(404, "技能不存在"));
    }

    @DeleteMapping("/admin/skills/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteSkill(@PathVariable Integer id) {
        return skillRepository.findById(id)
                .map(skill -> {
                    skillRepository.delete(skill);
                    return Result.success("技能删除成功");
                })
                .orElse(Result.error(404, "技能不存在"));
    }

    public static class SkillRequest {
        private String name;
        private Integer sortOrder;
        private String category;
        private Integer level;
        private String iconUrl;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public String getIconUrl() { return iconUrl; }
        public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    }
}