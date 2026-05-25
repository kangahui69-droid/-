-- =====================================================
-- TechForge 数据库完整表结构
-- 执行方式: source e:/java/portfolio/sql/tables.sql
-- 或: mysql -u root -p < e:/java/portfolio/sql/tables.sql
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS techforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE techforge;

-- ========== 1. 用户表 (管理员) ==========
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 2. 个人简介表 ==========
DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile` (
    `id` INT PRIMARY KEY,  -- 仅一条数据，固定 id=1
    `name` VARCHAR(50),
    `avatar` VARCHAR(200),
    `bio` VARCHAR(200),
    `about` TEXT,
    `social_github` VARCHAR(200),
    `social_email` VARCHAR(200),
    `social_x` VARCHAR(200),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 3. 博客文章表 ==========
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `slug` VARCHAR(200) NOT NULL UNIQUE,
    `content` TEXT NOT NULL,
    `excerpt` VARCHAR(500),
    `cover_image` VARCHAR(200),
    `status` ENUM('DRAFT','PUBLISHED') DEFAULT 'DRAFT',
    `views` INT DEFAULT 0,
    `likes` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME DEFAULT NULL,
    INDEX idx_slug (`slug`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 4. 标签表 ==========
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 5. 文章-标签中间表 (多对多) ==========
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
    `article_id` INT NOT NULL,
    `tag_id` INT NOT NULL,
    PRIMARY KEY (`article_id`, `tag_id`),
    FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 6. 项目展示表 ==========
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NOT NULL,
    `tech_stack` VARCHAR(300),
    `cover_image` VARCHAR(200),
    `demo_url` VARCHAR(200),
    `github_url` VARCHAR(200),
    `sort_order` INT DEFAULT 0,
    `status` ENUM('DRAFT','PUBLISHED') DEFAULT 'DRAFT',
    `project_type` VARCHAR(50),
    `features` JSON,
    `structure` TEXT,
    `summary` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME DEFAULT NULL,
    INDEX idx_status (`status`),
    INDEX idx_sort_order (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 7. 技能标签表 ==========
DROP TABLE IF EXISTS `skill`;
CREATE TABLE `skill` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 8. AI对话历史表 ==========
DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE `chat_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(50),
    `question` TEXT NOT NULL,
    `answer` TEXT,
    `tokens` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 9. 文章评论表 ==========
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `article_id` INT NOT NULL,
    `author` VARCHAR(50) NOT NULL DEFAULT '访客',
    `content` TEXT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_article (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化管理员 (密码: 666666)
INSERT INTO `user` (`username`, `password`) VALUES
('xiaohui', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS/lWdpRqH5FRXKVqq');

-- 初始化个人简介
INSERT INTO `profile` (`id`, `name`, `bio`, `about`) VALUES
(1, '小辉', 'RISC-V 物联网 · AI 开发者', '专注于边缘AI与工业智能应用开发。');

-- 初始化标签
INSERT INTO `tag` (`name`) VALUES
('RISC-V'), ('工业AI'), ('深度学习'), ('经验'), ('边缘计算');

-- 初始化示例文章
INSERT INTO `article` (`title`, `slug`, `content`, `excerpt`, `status`, `views`) VALUES
('基于RISC-V开发板的边缘AI部署实战', 'edge-ai-on-riscv',
'# 基于RISC-V开发板的边缘AI部署实战\n\n从零开始教你在全志D1s上运行TensorFlow Lite模型...',
'从零开始教你在全志D1s上运行TensorFlow Lite模型...', 'PUBLISHED', 0);

-- 初始化示例项目
INSERT INTO `project` (`name`, `description`, `tech_stack`, `status`, `sort_order`) VALUES
('工业设备异常溯源系统', '基于RISC-V开发板的多传感器实时监控系统，实现异常检测与因果推断溯源。', 'RISC-V, TensorFlow Lite, Flask', 'PUBLISHED', 1),
('英语单词学习APP', 'Android背单词应用，基于艾宾浩斯遗忘曲线算法。', 'Kotlin, Docker', 'PUBLISHED', 2);

-- 初始化技能
INSERT INTO `skill` (`name`, `sort_order`) VALUES
('RISC-V 开发', 1), ('C / Python', 2), ('TensorFlow Lite', 3),
('边缘计算', 4), ('IoT 物联网', 5), ('Flask / FastAPI', 6),
('Kotlin / Android', 7), ('Docker', 8), ('MySQL', 9), ('AI / 大模型', 10);

-- =====================================================
-- 执行完成后验证
-- =====================================================
SELECT '数据库创建完成!' AS result;
SHOW TABLES;