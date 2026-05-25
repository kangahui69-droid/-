package com.techforge.repository;

import com.techforge.entity.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Integer> {

    List<ProjectSkill> findByProjectId(Integer projectId);

    List<ProjectSkill> findBySkillId(Integer skillId);

    void deleteByProjectId(Integer projectId);
}