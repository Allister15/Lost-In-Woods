package com.DinoCorp.Lost_In_Woods.repository;

import com.DinoCorp.Lost_In_Woods.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByOwnerKey(String ownerKey);

    boolean existsByOwnerKeyAndAchievementId(String ownerKey, String achievementId);
}
