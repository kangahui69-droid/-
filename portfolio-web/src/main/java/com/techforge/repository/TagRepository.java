package com.techforge.repository;

import com.techforge.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Tag Repository
 */
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);
}