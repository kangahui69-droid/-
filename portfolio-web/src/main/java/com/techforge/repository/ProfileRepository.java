package com.techforge.repository;

import com.techforge.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Profile Repository
 */
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
}