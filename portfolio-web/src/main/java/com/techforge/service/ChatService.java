package com.techforge.service;

import com.techforge.client.AiApiClient;
import com.techforge.entity.ChatHistory;
import com.techforge.entity.Profile;
import com.techforge.entity.Project;
import com.techforge.entity.Skill;
import com.techforge.entity.Project.ProjectStatus;
import com.techforge.repository.ChatHistoryRepository;
import com.techforge.repository.ProfileRepository;
import com.techforge.repository.ProjectRepository;
import com.techforge.repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 对话业务逻辑服务
 * - RAG 上下文构建
 * - Prompt 组装
 * - 历史记录管理
 */
@Service
public class ChatService {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final AiApiClient aiApiClient;

    public ChatService(ProfileRepository profileRepository,
                      ProjectRepository projectRepository,
                      SkillRepository skillRepository,
                      ChatHistoryRepository chatHistoryRepository,
                      AiApiClient aiApiClient) {
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.aiApiClient = aiApiClient;
    }

    /**
     * 处理对话请求
     * @param question 用户问题
     * @param sessionId 会话ID（可选）
     * @return AI 回复
     */
    public String chat(String question, String sessionId) {
        // 1. 构建上下文
        String context = buildContext();

        // 2. 构建 Prompt
        String systemPrompt = buildSystemPrompt(context);
        String userPrompt = "请问: " + question;

        // 3. 调用 AI
        String answer = aiApiClient.call(systemPrompt, userPrompt);

        // 4. 保存历史
        if (sessionId != null && !sessionId.isEmpty()) {
            saveHistory(sessionId, question, answer);
        }

        return answer;
    }

    /**
     * 构建 RAG 上下文
     */
    private String buildContext() {
        StringBuilder sb = new StringBuilder();

        // 个人简介
        profileRepository.findById(1).ifPresent(p -> {
            sb.append("关于我:\n");
            sb.append("- 名字: ").append(aiApiClient.safeStr(p.getName())).append("\n");
            sb.append("- 简介: ").append(aiApiClient.safeStr(p.getBio())).append("\n");
            if (p.getAbout() != null && !p.getAbout().isEmpty()) {
                sb.append("- 详细介绍: ").append(aiApiClient.safeStr(p.getAbout())).append("\n");
            }
        });

        // 项目列表（Top 5）
        List<Project> projects = projectRepository
                .findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(ProjectStatus.PUBLISHED)
                .stream().limit(5).collect(Collectors.toList());
        if (!projects.isEmpty()) {
            sb.append("\n我做过的项目:\n");
            for (Project p : projects) {
                sb.append("- ").append(aiApiClient.safeStr(p.getName())).append(": ")
                  .append(aiApiClient.safeStr(p.getDescription())).append("\n");
                sb.append("  技术栈: ").append(aiApiClient.safeStr(p.getTechStack())).append("\n");
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

    /**
     * 构建系统 Prompt
     */
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

    /**
     * 保存历史记录
     */
    private void saveHistory(String sessionId, String question, String answer) {
        ChatHistory history = new ChatHistory();
        history.setSessionId(sessionId);
        history.setQuestion(question);
        history.setAnswer(answer);
        chatHistoryRepository.save(history);
    }
}