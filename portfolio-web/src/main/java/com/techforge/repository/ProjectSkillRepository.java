package com.techforge.repository;

import com.techforge.entity.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Integer> {

    List<ProjectSkill> findByProjectId(Integer projectId);

    List<ProjectSkill> findBySkillId(Integer skillId);

    void deleteByProjectId(Integer projectId);

    @Query("SELECT ps FROM ProjectSkill ps WHERE ps.projectId IN :projectIds")
    List<ProjectSkill> findByProjectIds(@Param("projectIds") List<Integer> projectIds);
}