package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Comment;
import com.techforge.repository.CommentRepository;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * 评论列表 (公开)
     */
    @GetMapping
    public Result<Map<String, Object>> getComments(
            @RequestParam Integer articleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Comment> comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(
                articleId,
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        );

        List<Map<String, Object>> list = comments.getContent().stream()
                .map(this::toMap)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", comments.getTotalElements());
        data.put("page", page);
        data.put("size", size);

        return Result.success(data);
    }

    /**
     * 发表评论 (公开，无需登录)
     */
    @PostMapping
    public Result<?> addComment(@RequestBody CommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return Result.error(400, "评论内容不能为空");
        }
        if (request.getArticleId() == null) {
            return Result.error(400, "文章ID不能为空");
        }

        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        // XSS 防护
        comment.setAuthor(StringEscapeUtils.escapeHtml4(request.getAuthor() != null ? request.getAuthor() : "访客"));
        comment.setContent(StringEscapeUtils.escapeHtml4(request.getContent()));

        commentRepository.save(comment);
        return Result.success("评论成功");
    }

    /**
     * 删除评论 (管理)
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteComment(@PathVariable Integer id) {
        return commentRepository.findById(id)
                .map(comment -> {
                    commentRepository.delete(comment);
                    return Result.success("删除成功");
                })
                .orElse(Result.error(404, "评论不存在"));
    }

    private Map<String, Object> toMap(Comment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("articleId", comment.getArticleId());
        map.put("author", comment.getAuthor());
        map.put("content", comment.getContent());
        map.put("createdAt", comment.getCreatedAt());
        return map;
    }

    public static class CommentRequest {
        private Integer articleId;
        private String author;
        private String content;

        public Integer getArticleId() { return articleId; }
        public void setArticleId(Integer articleId) { this.articleId = articleId; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}