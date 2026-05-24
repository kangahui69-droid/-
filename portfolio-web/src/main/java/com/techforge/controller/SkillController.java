package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Skill;
import com.techforge.repository.SkillRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        skillRepository.save(skill);
        return Result.success("技能添加成功");
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

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}