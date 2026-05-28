package com.techforge.controller;

import com.techforge.config.JwtUtils;
import com.techforge.dto.Result;
import com.techforge.entity.Article;
import com.techforge.entity.Article.ArticleStatus;
import com.techforge.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章接口
 */
@RestController
@RequestMapping("/api")
public class ArticleController {

    private final ArticleRepository articleRepository;

    public ArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /**
     * 文章列表 (公开)
     */
    @GetMapping("/articles")
    public Result<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Article> articles = articleRepository.findByStatusAndDeletedAtIsNull(
                ArticleStatus.PUBLISHED,
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        );

        List<Map<String, Object>> list = articles.getContent().stream()
                .map(this::toSummary)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", articles.getTotalElements());
        data.put("page", page);
        data.put("size", size);

        return Result.success(data);
    }

    /**
     * 文章详情 (公开)
     */
    @GetMapping("/articles/{id}")
    public Result<Article> getArticle(@PathVariable Integer id) {
        return articleRepository.findByIdAndDeletedAtIsNull(id)
                .map(article -> {
                    article.setViews(article.getViews() + 1);
                    articleRepository.save(article);
                    return Result.success(article);
                })
                .orElse(Result.error(404, "文章不存在"));
    }

    // ========== 管理接口 ==========

    @PostMapping("/articles")
    @PreAuthorize("isAuthenticated()")
    public Result<?> createArticle(@RequestBody ArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setContent(request.getContent());
        article.setExcerpt(request.getExcerpt());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus() != null ? request.getStatus() : ArticleStatus.DRAFT);

        articleRepository.save(article);
        return Result.success("文章创建成功");
    }

    @PutMapping("/articles/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updateArticle(@PathVariable Integer id, @RequestBody ArticleRequest request) {
        return articleRepository.findById(id)
                .map(article -> {
                    if (request.getTitle() != null) article.setTitle(request.getTitle());
                    if (request.getSlug() != null) article.setSlug(request.getSlug());
                    if (request.getContent() != null) article.setContent(request.getContent());
                    if (request.getExcerpt() != null) article.setExcerpt(request.getExcerpt());
                    if (request.getCoverImage() != null) article.setCoverImage(request.getCoverImage());
                    if (request.getStatus() != null) article.setStatus(request.getStatus());

                    articleRepository.save(article);
                    return Result.success("文章更新成功");
                })
                .orElse(Result.error(404, "文章不存在"));
    }

    @DeleteMapping("/articles/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteArticle(@PathVariable Integer id) {
        return articleRepository.findById(id)
                .map(article -> {
                    article.setDeletedAt(LocalDateTime.now());
                    articleRepository.save(article);
                    return Result.success("文章删除成功");
                })
                .orElse(Result.error(404, "文章不存在"));
    }

    @GetMapping("/admin/articles/all")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getAllArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Article> articles = articleRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        );

        List<Map<String, Object>> list = articles.getContent().stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("title", a.getTitle());
                    map.put("slug", a.getSlug());
                    map.put("status", a.getStatus());
                    map.put("views", a.getViews());
                    map.put("createdAt", a.getCreatedAt());
                    return map;
                })
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", articles.getTotalElements());
        data.put("page", page);
        data.put("size", size);

        return Result.success(data);
    }

    private Map<String, Object> toSummary(Article article) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", article.getId());
        map.put("title", article.getTitle());
        map.put("slug", article.getSlug());
        map.put("excerpt", article.getExcerpt());
        map.put("coverImage", article.getCoverImage());
        map.put("views", article.getViews());
        map.put("likes", article.getLikes());
        map.put("createdAt", article.getCreatedAt());
        return map;
    }

    public static class ArticleRequest {
        private String title;
        private String slug;
        private String content;
        private String excerpt;
        private String coverImage;
        private ArticleStatus status;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getExcerpt() { return excerpt; }
        public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
        public ArticleStatus getStatus() { return status; }
        public void setStatus(ArticleStatus status) { this.status = status; }
    }
}