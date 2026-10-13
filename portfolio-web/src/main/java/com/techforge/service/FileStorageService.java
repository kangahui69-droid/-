package com.techforge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 上传文件存储 - 负责落盘、删除与路径解析
 */
@Service
public class FileStorageService {

    private static final String URL_PREFIX = "/uploads/";

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 写入 uploads 目录，返回存储文件名
     */
    public String store(byte[] content, String storedName) throws IOException {
        Files.createDirectories(uploadDir);
        Files.write(uploadDir.resolve(storedName), content);
        return storedName;
    }

    /**
     * 由 URL(/uploads/xxx) 解析磁盘路径；非法或越界返回 null
     */
    public Path resolve(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) return null;
        String filename = url.substring(URL_PREFIX.length());
        if (filename.isEmpty() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return null;
        }
        return uploadDir.resolve(filename);
    }

    /**
     * 删除 URL 对应文件（不存在或非 /uploads/ 外链则忽略）
     */
    public void delete(String url) {
        Path path = resolve(url);
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 删除失败不影响业务
        }
    }

    public String toUrl(String storedName) {
        return URL_PREFIX + storedName;
    }
}
