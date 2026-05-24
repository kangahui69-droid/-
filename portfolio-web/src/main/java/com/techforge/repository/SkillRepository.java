package com.techforge.repository;

import com.techforge.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Skill Repository
 */
public interface SkillRepository extends JpaRepository<Skill, Integer> {

    /**
     * 按排序查询
     */
    List<Skill> findAllByOrderBySortOrderAsc();
}