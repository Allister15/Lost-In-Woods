package com.LostInWoods.app.repository;

import com.LostInWoods.app.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Scene entity
 * Follows Repository Pattern for data access abstraction
 */
@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {

    /**
     * Find a scene by title
     */
    Optional<Scene> findByTitle(String title);
}
