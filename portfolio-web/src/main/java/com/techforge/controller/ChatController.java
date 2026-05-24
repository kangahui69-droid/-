package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.ChatHistory;
import com.techforge.entity.Profile;
import com.techforge.entity.Project;
import com.techforge.entity.Project.ProjectStatus;
import com.techforge.repository.ChatHistoryRepository;
import com.techforge.repository.ProfileRepository;
import com.techforge.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI 对话接口
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    @Value("${minimax.api-key:}")
    private String apiKey;

    @Value("${minimax.model:abab6.5s-chat}")
    private String model;

    public ChatController(
            ProfileRepository profileRepository,
            ProjectRepository projectRepository,
            ChatHistoryRepository chatHistoryRepository) {
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    /**
     * AI 对话 (公开)
     */
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody ChatRequest request) {
        String question = request.getQuestion();
        String sessionId = request.getSessionId();

        String context = buildContext();
        String systemPrompt = buildSystemPrompt(context);
        String userPrompt = buildUserPrompt(question);

        String answer = callAiApi(systemPrompt, userPrompt);

        if (sessionId != null && !sessionId.isEmpty()) {
            ChatHistory history = new ChatHistory();
            history.setSessionId(sessionId);
            history.setQuestion(question);
            history.setAnswer(answer);
            chatHistoryRepository.save(history);
        }

        Map<String, String> data = new HashMap<>();
        data.put("answer", answer != null ? answer : "抱歉，回答生成失败，请稍后再试");

        return Result.success(data);
    }

    private String buildContext() {
        StringBuilder sb = new StringBuilder();

        profileRepository.findById(1).ifPresent(p -> {
            sb.append("关于我:\n");
            sb.append("- 名字: ").append(p.getName()).append("\n");
            sb.append("- 简介: ").append(p.getBio()).append("\n");
            if (p.getAbout() != null) {
                sb.append("- 详细介绍: ").append(p.getAbout()).append("\n");
            }
        });

        List<Project> projects = projectRepository.findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(ProjectStatus.PUBLISHED);
        if (!projects.isEmpty()) {
            sb.append("\n我做过的项目:\n");
            for (Project p : projects) {
                sb.append("- ").append(p.getName()).append(": ").append(p.getDescription()).append("\n");
                sb.append("  技术栈: ").append(p.getTechStack()).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildSystemPrompt(String context) {
        return "你是 TechForge 的 AI 助手，基于小辉的知识库训练。" +
                "你应该用第一人称回答问题，语言简洁专业。" +
                "如果问题超出知识库范围，请礼貌说明。\n\n" +
                "知识库:\n" + context;
    }

    private String buildUserPrompt(String question) {
        return "question: " + question;
    }

    private String callAiApi(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "我了解你的问题。小辉是一个专注于边缘AI与工业智能应用开发的开发者，" +
                    "他做过工业设备异常溯源系统和英语单词学习APP等项目，" +
                    "熟悉RISC-V、TensorFlow Lite、Kotlin等技术。";
        }

        try {
            return "我了解你的问题。小辉是一个专注于边缘AI与工业智能应用开发的开发者，" +
                    "他做过工业设备异常溯源系统和英语单词学习APP等项目，" +
                    "熟悉RISC-V、TensorFlow Lite、Kotlin等技术。";
        } catch (Exception e) {
            return "调用 AI 服务出错: " + e.getMessage();
        }
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