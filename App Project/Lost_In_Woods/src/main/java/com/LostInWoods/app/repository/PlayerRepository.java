package com.LostInWoods.app.repository;

import com.LostInWoods.app.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Player entity
 * Follows Repository Pattern for data access abstraction
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Find a player by name
     */
    Optional<Player> findByName(String name);
}
