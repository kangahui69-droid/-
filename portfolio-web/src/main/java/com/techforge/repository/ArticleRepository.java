package com.techforge.repository;

import com.techforge.entity.Article;
import com.techforge.entity.Article.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Article Repository
 */
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    /**
     * 查询已发布的文章 (分页)
     */
    Page<Article> findByStatusAndDeletedAtIsNull(ArticleStatus status, Pageable pageable);

    /**
     * 根据ID查询 (排除软删除)
     */
    Optional<Article> findByIdAndDeletedAtIsNull(Integer id);

    /**
     * 根据Slug查询 (排除软删除)
     */
    Optional<Article> findBySlugAndDeletedAtIsNull(String slug);
}