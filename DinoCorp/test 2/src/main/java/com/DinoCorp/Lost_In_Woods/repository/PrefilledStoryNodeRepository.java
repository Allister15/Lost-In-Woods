package com.DinoCorp.Lost_In_Woods.repository;

import com.DinoCorp.Lost_In_Woods.model.PrefilledStoryNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrefilledStoryNodeRepository extends JpaRepository<PrefilledStoryNode, Long> {

    // The O(1) funnel seek — backed by the uq_session_node (session_id, node_index) index.
    Optional<PrefilledStoryNode> findBySessionIdAndNodeIndex(String sessionId, int nodeIndex);

    List<PrefilledStoryNode> findBySessionIdOrderByNodeIndexAsc(String sessionId);

    long countBySessionId(String sessionId);

    // Rolling-pipeline readiness probe: a chapter is fully generated once it has all
    // 4 of its turns persisted. Cheap COUNT, used to decide whether to (pre)generate.
    long countBySessionIdAndChapterNumber(String sessionId, int chapterNumber);

    void deleteBySessionId(String sessionId);
}
