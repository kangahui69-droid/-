package com.techforge.controller;

import com.techforge.dto.Result;
import com.techforge.entity.Profile;
import com.techforge.repository.ProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Profile 接口
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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