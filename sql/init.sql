-- TechForge 数据库初始化脚本
-- 执行前请确保 MySQL 已安装并运行

-- 创建数据库
CREATE DATABASE IF NOT EXISTS techforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE techforge;

-- ========== 注意 ==========
-- 下面的表会在应用启动时由 JPA 自动创建
-- 仅作参考，已注释掉

-- ========== 初始化管理员 ==========
-- 密码: xiaohui (BCrypt 加密)
-- 在线生成 BCrypt 密码: https://bcrypt.online/

-- INSERT INTO user (username, password) VALUES ('xiaohui', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS/lWdpRqH5FRXKVqq');
-- 上面密码是 "666666" 的 BCrypt 加密

-- ========== 初始化个人简介 ==========
-- INSERT INTO profile (id, name, bio, about) VALUES (1, '小辉', 'RISC-V 物联网 · AI 爱好者', '专注于边缘AI与工业智能应用开发');

-- ========== 示例文章 ==========
-- INSERT INTO article (title, slug, content, excerpt, status, views) VALUES
-- ('基于RISC-V开发板的边缘AI部署实战', 'edge-ai-on-riscv', '# 基于RISC-V开发板的边缘AI部署实战\n\n从小开始...', '从零开始教你在全志D1s上运行TensorFlow Lite模型...', 'PUBLISHED', 0),
-- ('因果推断在工业异常检测中的应用', 'causal-inference-industrial', '# 因果推断在工业异常检测中的应用\n\n格兰杰...', '探讨如何利用格兰杰因果检验识别多传感器异常...', 'PUBLISHED', 0);

-- ========== 示例项目 ==========
-- INSERT INTO project (name, description, tech_stack, sort_order, status) VALUES
-- ('工业设备异常溯源系统', '基于RISC-V开发板的多传感器实时监控系统...', 'RISC-V, TensorFlow Lite, Flask, MQTT', 1, 'PUBLISHED'),
-- ('英语单词学习APP', 'Android平台背单词应用...', 'Kotlin, Jetpack Compose, Room', 2, 'PUBLISHED');

-- ========== 示例技能 ==========
-- INSERT INTO skill (name, sort_order) VALUES
-- ('RISC-V 开发', 1),
-- ('C / Python', 2),
-- ('TensorFlow Lite', 3),
-- ('边缘计算', 4),
-- ('IoT 物联网', 5),
-- ('Flask / FastAPI', 6),
-- ('Kotlin / Android', 7),
-- ('Docker', 8),
-- ('MySQL', 9),
-- ('AI / 大模型', 10);