package com.techforge.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI API 客户端 - 负责 MiniMax API 调用、fallback 逻辑、响应解析
 */
@Component
public class AiApiClient {

    @Value("${ai.minimax.key:}")
    private String apiKey;

    @Value("${minimax.model:abab6.5s-chat}")
    private String model;

    @Value("${minimax.api-url:https://api.minimax.chat/v1/text/chatcompletion_pro}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public AiApiClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 调用 AI API
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户问题
     * @return AI 回复
     */
    public String call(String systemPrompt, String userPrompt) {
        // 无 API Key 时返回 fallback
        if (apiKey == null || apiKey.isEmpty()) {
            return FALLBACK_TEXT;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);

            // OpenAI 兼容格式消息
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

            return "抱歉，未收到有效���复，请稍後再试";
        } catch (Exception e) {
            return "服务繁忙，请稍后重试: " + e.getMessage();
        }
    }

    /**
     * 解析多种可能的响应结构
     */
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

    /**
     * 字符串安全处理
     */
    public String safeStr(String s) {
        return s != null ? s : "未设置";
    }

    private static final String FALLBACK_TEXT =
            "我是 TechForge 的技术顾问助手。我熟悉 Java、Python、RISC-V 开发等技术，\n" +
            "做过工业设备异常溯源系统、英语单词学习 APP 等项目。\n" +
            "如果你对我的技术能力或项目经验感兴趣，可以问我具体问题哦~";
}