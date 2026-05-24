package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Profile;
import com.techforge.repository.ProfileRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开接口 - 个人简介
 */
@RestController
@RequestMapping("/api")
public class PublicController {

    private final ProfileRepository profileRepository;

    public PublicController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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
            data.put("social", Map.of("github", "", "email", "", "x", ""));
        }

        return Result.success(data);
    }
}