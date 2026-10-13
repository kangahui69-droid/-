package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Profile;
import com.techforge.repository.ProfileRepository;
import com.techforge.service.FileStorageService;
import com.techforge.util.ImageTypeDetector;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Profile 接口
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    private static final long MAX_RESUME_SIZE = 10 * 1024 * 1024;
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;

    private final ProfileRepository profileRepository;
    private final FileStorageService fileStorageService;

    public ProfileController(ProfileRepository profileRepository, FileStorageService fileStorageService) {
        this.profileRepository = profileRepository;
        this.fileStorageService = fileStorageService;
    }

    // ========== 管理接口 ==========

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updateProfile(@RequestBody ProfileRequest request) {
        Profile profile = profileRepository.findById(1).orElseGet(() -> {
            Profile p = new Profile();
            p.setId(1);
            return p;
        });

        if (request.getName() != null) profile.setName(request.getName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getAbout() != null) profile.setAbout(request.getAbout());
        if (request.getAvatar() != null) profile.setAvatar(request.getAvatar());
        if (request.getSocial() != null) {
            Map<String, String> social = request.getSocial();
            if (social.containsKey("github")) profile.setSocialGithub(social.get("github"));
            if (social.containsKey("email")) profile.setSocialEmail(social.get("email"));
            if (social.containsKey("x")) profile.setSocialX(social.get("x"));
        }

        profileRepository.save(profile);
        return Result.success("个人简介更新成功");
    }

    // ========== 简历 ==========

    /**
     * 上传简历（PDF，覆盖旧文件）
     */
    @PostMapping("/profile/resume")
    @PreAuthorize("isAuthenticated()")
    public Result<?> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }
        if (file.getSize() > MAX_RESUME_SIZE) {
            return Result.error(400, "文件不能超过 10MB");
        }
        String original = file.getOriginalFilename();
        if (original == null || !original.toLowerCase().endsWith(".pdf")
                || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            return Result.error(400, "只支持 PDF 格式的简历");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            return Result.error(500, "读取文件失败");
        }
        if (content.length < 5 || !"%PDF-".equals(new String(content, 0, 5, StandardCharsets.US_ASCII))) {
            return Result.error(400, "文件内容不是有效的 PDF");
        }

        Profile profile = profileRepository.findById(1).orElseGet(() -> {
            Profile p = new Profile();
            p.setId(1);
            return p;
        });

        String storedName;
        try {
            storedName = fileStorageService.store(content, "resume-" + System.currentTimeMillis() + ".pdf");
        } catch (IOException e) {
            return Result.error(500, "保存文件失败");
        }

        fileStorageService.delete(profile.getResumeUrl());
        profile.setResumeUrl(fileStorageService.toUrl(storedName));
        profile.setResumeName(sanitizeFilename(original));
        profile.setResumeAt(LocalDateTime.now());
        profileRepository.save(profile);

        Map<String, Object> data = new HashMap<>();
        data.put("resumeName", profile.getResumeName());
        data.put("resumeAt", profile.getResumeAt());
        return Result.success(data);
    }

    /**
     * 删除简历
     */
    @DeleteMapping("/profile/resume")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteResume() {
        Profile profile = profileRepository.findById(1).orElse(null);
        if (profile == null || profile.getResumeUrl() == null) {
            return Result.error(404, "当前没有简历");
        }
        fileStorageService.delete(profile.getResumeUrl());
        profile.setResumeUrl(null);
        profile.setResumeName(null);
        profile.setResumeAt(null);
        profileRepository.save(profile);
        return Result.success("简历已删除");
    }

    // ========== 头像 ==========

    /**
     * 上传头像（JPG/PNG/WEBP，覆盖旧文件）
     */
    @PostMapping("/profile/avatar")
    @PreAuthorize("isAuthenticated()")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            return Result.error(400, "图片不能超过 2MB");
        }
        String original = file.getOriginalFilename();
        if (original == null || !original.toLowerCase().matches(".*\\.(jpg|jpeg|png|webp)$")) {
            return Result.error(400, "只支持 JPG/PNG/WEBP 格式的图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/webp"))) {
            return Result.error(400, "只支持 JPG/PNG/WEBP 格式的图片");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            return Result.error(500, "读取文件失败");
        }

        String ext = ImageTypeDetector.detect(content);
        if (ext == null) {
            return Result.error(400, "图片内容不是有效的 JPG/PNG/WEBP");
        }

        Profile profile = profileRepository.findById(1).orElseGet(() -> {
            Profile p = new Profile();
            p.setId(1);
            return p;
        });

        String storedName;
        try {
            storedName = fileStorageService.store(content, "avatar-" + System.currentTimeMillis() + "." + ext);
        } catch (IOException e) {
            return Result.error(500, "保存图片失败");
        }

        fileStorageService.delete(profile.getAvatar());
        profile.setAvatar(fileStorageService.toUrl(storedName));
        profileRepository.save(profile);

        Map<String, Object> data = new HashMap<>();
        data.put("avatar", profile.getAvatar());
        return Result.success(data);
    }

    /**
     * 删除头像
     */
    @DeleteMapping("/profile/avatar")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteAvatar() {
        Profile profile = profileRepository.findById(1).orElse(null);
        if (profile == null || profile.getAvatar() == null || profile.getAvatar().isEmpty()) {
            return Result.error(404, "当前没有头像");
        }
        fileStorageService.delete(profile.getAvatar());
        profile.setAvatar(null);
        profileRepository.save(profile);
        return Result.success("头像已删除");
    }

    private String sanitizeFilename(String name) {
        String base = name.replace("\\", "/");
        int idx = base.lastIndexOf('/');
        if (idx >= 0) base = base.substring(idx + 1);
        base = base.replaceAll("[\\r\\n\"]", "").trim();
        return base.isEmpty() ? "resume.pdf" : base;
    }

    public static class ProfileRequest {
        private String name;
        private String bio;
        private String about;
        private String avatar;
        private Map<String, String> social;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public String getAbout() { return about; }
        public void setAbout(String about) { this.about = about; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public Map<String, String> getSocial() { return social; }
        public void setSocial(Map<String, String> social) { this.social = social; }
    }
}