package com.ambience.TransactionsService.model.entity;

import com.ambience.TransactionsService.model.enums.ChunkStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chunks", schema = "public")
public class ChunksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobsEntity job;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false)
    private String hash;

    @Column(name = "audio_path", nullable = false)
    private String audioPath;

    @Column(nullable = true)
    private String transcription;

    @Column(name = "chunk_status", nullable = false)
    private ChunkStatus chunkStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "completed_at", nullable = true)
    private OffsetDateTime completedAt;

    @PrePersist
    private void createdAtTimestamp() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    private void updatedAtTimestamp() {
        updatedAt = OffsetDateTime.now();
    }
}
