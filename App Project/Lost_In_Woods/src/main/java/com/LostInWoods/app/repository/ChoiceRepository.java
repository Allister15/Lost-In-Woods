package com.LostInWoods.app.repository;

import com.LostInWoods.app.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Choice entity
 * Follows Repository Pattern for data access abstraction
 */
@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    /**
     * Find all choices for a specific scene
     */
    List<Choice> findBySceneIdOrderByDisplayOrder(Long sceneId);
}
