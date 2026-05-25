package com.techforge.dto;

import java.util.List;

/**
 * 技能树DTO
 */
public class SkillTreeDTO {

    private List<Category> categories;

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }

    public static class Category {
        private String id;
        private String name;
        private List<SkillVO> skills;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<SkillVO> getSkills() { return skills; }
        public void setSkills(List<SkillVO> skills) { this.skills = skills; }
    }

    public static class SkillVO {
        private Integer id;
        private String name;
        private Integer level;
        private String icon;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}