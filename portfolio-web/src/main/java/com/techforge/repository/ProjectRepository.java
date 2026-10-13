package com.techforge.repository;

import com.techforge.entity.Project;
import com.techforge.entity.Project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Project Repository
 */
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    /**
     * 查询已发布的项目 (按排序, 排除已删除)
     */
    List<Project> findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(ProjectStatus status);

    /**
     * 查询全部未删除项目 (后台管理用, 含草稿)
     */
    List<Project> findByDeletedAtIsNullOrderBySortOrderAsc();
}