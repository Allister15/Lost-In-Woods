package com.LostInWoods.app.repository;

import com.LostInWoods.app.entity.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for GameProgress entity
 * Follows Repository Pattern for data access abstraction
 */
@Repository
public interface GameProgressRepository extends JpaRepository<GameProgress, Long> {

    /**
     * Find the latest game progress for a player
     */
    Optional<GameProgress> findFirstByPlayerIdOrderByLastSavedAtDesc(Long playerId);
}
