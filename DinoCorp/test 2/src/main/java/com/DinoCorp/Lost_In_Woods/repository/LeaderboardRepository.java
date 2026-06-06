package com.DinoCorp.Lost_In_Woods.repository;

import com.DinoCorp.Lost_In_Woods.model.LeaderboardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardRecord, String> {

    // Top registered accounts by their best historical score.
    List<LeaderboardRecord> findTop10ByOrderByBestScoreDesc();
}
