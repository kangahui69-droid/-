package com.techforge.exception;

import com.techforge.dto.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把控制器抛出的异常转成统一的 Result JSON，
 * 避免异常冒泡到 /error 被安全过滤器拦成空响应，前端误报"网络错误"。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrity(DataIntegrityViolationException e) {
        String raw = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : null;
        String message = "数据保存失败，请检查填写内容";
        if (raw != null && raw.contains("Duplicate entry")) {
            if (raw.contains("slug")) {
                message = "该 slug 已存在，请换一个";
            } else {
                message = "存在重复数据，无法保存";
            }
        }
        return Result.error(400, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(400, e.getMessage() != null ? e.getMessage() : "参数错误");
    }
}
