package com.techforge.repository;

import com.techforge.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Comment Repository
 */
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    /**
     * 查询某文章的评论 (分页，按时间倒序)
     */
    Page<Comment> findByArticleIdOrderByCreatedAtDesc(Integer articleId, Pageable pageable);

    /**
     * 统计某文章的评论数
     */
    long countByArticleId(Integer articleId);
}