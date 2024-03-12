package com.ambience.TransactionsService.model.repository;

import com.ambience.TransactionsService.model.entity.ChunksEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ChunksRepository extends JpaRepository<ChunksEntity, Object> {
    // You can define custom methods here

    @Query("SELECT c FROM ChunksEntity c WHERE c.updatedAt < :cutoffTime AND c.chunkStatus NOT IN (2, 3)")
    List<ChunksEntity> findLostChunks(OffsetDateTime cutoffTime);
}