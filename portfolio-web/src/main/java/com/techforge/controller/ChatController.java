package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.ChatHistory;
import com.techforge.repository.ChatHistoryRepository;
import com.techforge.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 对话接口 - 仅路由层
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final ChatHistoryRepository chatHistoryRepository;

    public ChatController(ChatService chatService,
                         ChatHistoryRepository chatHistoryRepository) {
        this.chatService = chatService;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    /**
     * AI 对话 (公开)
     */
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody ChatRequest request) {
        String answer = chatService.chat(request.getQuestion(), request.getSessionId());

        Map<String, String> data = new HashMap<>();
        data.put("answer", answer != null ? answer : "抱歉，回答生成失败，请稍后再试");

        return Result.success(data);
    }

    /**
     * 获取会话历史记录
     */
    @GetMapping("/chat/history/{sessionId}")
    public Result<List<ChatHistory>> getHistory(@PathVariable String sessionId) {
        List<ChatHistory> history = chatHistoryRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        return Result.success(history);
    }

    /**
     * 删除单条聊天记录
     */
    @DeleteMapping("/chat/history/{id}")
    public Result<?> deleteHistory(@PathVariable Integer id) {
        chatHistoryRepository.deleteById(id);
        return Result.success("删除成功");
    }

    public static class ChatRequest {
        private String question;
        private String sessionId;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}