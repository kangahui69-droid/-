package com.techforge.repository;

import com.techforge.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * ChatHistory Repository
 */
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {

    /**
     * 按会话ID查询 (最近20条)
     */
    List<ChatHistory> findTop20BySessionIdOrderByCreatedAtDesc(String sessionId);
}