package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.ChatHistory;
import com.techforge.entity.Profile;
import com.techforge.entity.Project;
import com.techforge.entity.Skill;
import com.techforge.entity.Project.ProjectStatus;
import com.techforge.repository.ChatHistoryRepository;
import com.techforge.repository.ProfileRepository;
import com.techforge.repository.ProjectRepository;
import com.techforge.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 对话接口 - 技术顾问助手
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.minimax.key}")
    private String apiKey;

    @Value("${minimax.model:abab6.5s-chat}")
    private String model;

    @Value("${minimax.api-url:https://api.minimax.chat/v1/text/chatcompletion_pro}")
    private String apiUrl;

    public ChatController(
            ProfileRepository profileRepository,
            ProjectRepository projectRepository,
            SkillRepository skillRepository,
            ChatHistoryRepository chatHistoryRepository) {
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.restTemplate = new RestTemplate();
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

        // 个人简介（添加null检查）
        profileRepository.findById(1).ifPresent(p -> {
            sb.append("关于我:\n");
            sb.append("- 名字: ").append(safeStr(p.getName())).append("\n");
            sb.append("- 简介: ").append(safeStr(p.getBio())).append("\n");
            if (p.getAbout() != null && !p.getAbout().isEmpty()) {
                sb.append("- 详细介绍: ").append(safeStr(p.getAbout())).append("\n");
            }
        });

        // 项目列表（Top 5，按排序）
        List<Project> projects = projectRepository
                .findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(ProjectStatus.PUBLISHED)
                .stream().limit(5).collect(Collectors.toList());
        if (!projects.isEmpty()) {
            sb.append("\n我做过的项目:\n");
            for (Project p : projects) {
                sb.append("- ").append(safeStr(p.getName())).append(": ").append(safeStr(p.getDescription())).append("\n");
                sb.append("  技术栈: ").append(safeStr(p.getTechStack())).append("\n");
            }
        }

        // 技能列表（按分类分组）
        List<Skill> skills = skillRepository.findAllByOrderBySortOrderAsc();
        if (!skills.isEmpty()) {
            sb.append("\n我的技术栈:\n");
            Map<String, List<Skill>> grouped = skills.stream()
                    .collect(Collectors.groupingBy(s -> s.getCategory() != null ? s.getCategory() : "other"));

            Map<String, String> categoryNames = new LinkedHashMap<>();
            categoryNames.put("frontend", "前端开发");
            categoryNames.put("backend", "后端开发");
            categoryNames.put("ai", "人工智能");
            categoryNames.put("hardware", "硬件/嵌入式");
            categoryNames.put("other", "其他");

            for (Map.Entry<String, String> entry : categoryNames.entrySet()) {
                String catId = entry.getKey();
                String catName = entry.getValue();
                List<Skill> catSkills = grouped.get(catId);
                if (catSkills != null && !catSkills.isEmpty()) {
                    sb.append("- ").append(catName).append(": ");
                    sb.append(catSkills.stream().map(Skill::getName).collect(Collectors.joining(", ")));
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    // 字符串安全处理
    private String safeStr(String s) {
        return s != null ? s : "未设置";
    }

    private String buildSystemPrompt(String context) {
        return "你是 TechForge 的 AI 技术顾问助手，专门帮助访客了解站主（小辉）的技术能力、项目经验和技术栈。\n\n" +
                "【角色定位】\n" +
                "- 你是一个专业技术顾问，熟悉软件开发、边缘AI、物联网等领域\n" +
                "- 用第一人称回答问题，语言简洁专业\n" +
                "- 如果问题超出技术范畴，礼貌说明并引导回到技术话题\n\n" +
                "【回答原则】\n" +
                "- 只回答技术相关问题，如编程、开发、项目经验、技术栈等\n" +
                "- 非技术问题（如天气、情感、娱乐）统一回复：「我是技术顾问，只回答技术相关问题哦~ 如果你想了解我的技术能力或项目经验，欢迎提问！」」\n" +
                "- 项目经验要包含名称、简介、技术栈\n" +
                "- 技能要说明熟练度和应用场景\n\n" +
                "【知识库】\n" + context;
    }

    private String buildUserPrompt(String question) {
        return "请问: " + question;
    }

    private String callAiApi(String systemPrompt, String userPrompt) {
        // 无 API Key 时返回 fallback
        if (apiKey == null || apiKey.isEmpty()) {
            return "我是 TechForge 的技术顾问助手。我熟悉 Java、Python、RISC-V 开发等技术，\n" +
                    "做过工业设备异常溯源系统、英语单词学习 APP 等项目。\n" +
                    "如果你对我的技术能力或项目经验感兴趣，可以问我具体问题哦~";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);

            // OpenAI 兼容格式：标准 role/content 消息
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> respBody = response.getBody();

                // OpenAI 兼容格式错误响应
                if (respBody.containsKey("error")) {
                    Map<String, Object> error = (Map<String, Object>) respBody.get("error");
                    return "API 错误: " + error.getOrDefault("message", error.get("code"));
                }

                // 兼容旧格式错误响应
                if (respBody.containsKey("base_resp")) {
                    Map<String, Object> baseResp = (Map<String, Object>) respBody.get("base_resp");
                    Object status = baseResp.get("status_code");
                    if (status != null && !status.equals(0)) {
                        String msg = (String) baseResp.get("status_msg");
                        return "API 错误: " + (msg != null ? msg : status);
                    }
                }

                String answer = parseAnswer(respBody);
                if (answer != null && !answer.isEmpty()) {
                    return answer;
                }

                return "抱歉，未收到有效回复，请稍后再试";
            }

            return "抱歉，未收到有效回复，请稍后再试";
        } catch (Exception e) {
            return "服务繁忙，请稍后重试: " + e.getMessage();
        }
    }

    // 解析多种可能的���应结构
    private String parseAnswer(Map<String, Object> respBody) {
        // 结构1: choices[0].message.content (abab 系列)
        if (respBody.containsKey("choices")) {
            Object choicesObj = respBody.get("choices");
            if (choicesObj instanceof List && !((List<?>) choicesObj).isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) choicesObj;
                Map<String, Object> choice = choices.get(0);
                if (choice != null) {
                    // 尝试 delta (流式/2.5格式)
                    if (choice.containsKey("delta")) {
                        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                        if (delta != null && delta.containsKey("content")) {
                            return (String) delta.get("content");
                        }
                    }
                    // 尝试 message (标准格式)
                    if (choice.containsKey("message")) {
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        if (message != null && message.containsKey("content")) {
                            return (String) message.get("content");
                        }
                    }
                }
            }
        }

        // 结构2: 直接在根级别
        if (respBody.containsKey("content")) {
            return (String) respBody.get("content");
        }

        // 结构3: message.content
        if (respBody.containsKey("message")) {
            Object msgObj = respBody.get("message");
            if (msgObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> msg = (Map<String, Object>) msgObj;
                return (String) msg.get("content");
            }
        }

        return null;
    }

    public static class ChatRequest {
        private String question;
        private String sessionId;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
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
}