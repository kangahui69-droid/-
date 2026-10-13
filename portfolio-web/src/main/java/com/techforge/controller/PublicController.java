package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Profile;
import com.techforge.repository.ProfileRepository;
import com.techforge.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 公开接口 - 个人简介
 */
@RestController
@RequestMapping("/api")
public class PublicController {

    private final ProfileRepository profileRepository;
    private final FileStorageService fileStorageService;

    public PublicController(ProfileRepository profileRepository, FileStorageService fileStorageService) {
        this.profileRepository = profileRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 获取个人简介
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        Profile profile = profileRepository.findById(1).orElse(null);

        Map<String, Object> data = new HashMap<>();
        if (profile != null) {
            data.put("name", profile.getName());
            data.put("bio", profile.getBio());
            data.put("about", profile.getAbout());
            data.put("avatar", profile.getAvatar());
            data.put("resumeName", profile.getResumeName());
            data.put("resumeAt", profile.getResumeAt());

            Map<String, String> social = new HashMap<>();
            social.put("github", profile.getSocialGithub());
            social.put("email", profile.getSocialEmail());
            social.put("x", profile.getSocialX());
            data.put("social", social);
        } else {
            data.put("name", "小辉");
            data.put("bio", "RISC-V 物联网 · AI 爱好者");
            data.put("about", "专注于边缘AI与工业智能应用开发");
            data.put("avatar", "");
            data.put("resumeName", null);
            data.put("resumeAt", null);
            data.put("social", Map.of("github", "", "email", "", "x", ""));
        }

        return Result.success(data);
    }

    /**
     * 查看简历（内联展示，支持在浏览器中下载）
     */
    @GetMapping("/profile/resume")
    public ResponseEntity<Resource> getResume() {
        Profile profile = profileRepository.findById(1).orElse(null);
        if (profile == null || profile.getResumeUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorageService.resolve(profile.getResumeUrl());
        if (path == null || !Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        String filename = profile.getResumeName() != null ? profile.getResumeName() : "resume.pdf";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"resume.pdf\"; filename*=UTF-8''" + encoded)
                .body(new FileSystemResource(path));
    }
}