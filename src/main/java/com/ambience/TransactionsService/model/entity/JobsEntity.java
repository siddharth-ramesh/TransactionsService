package com.ambience.TransactionsService.model.entity;

import com.ambience.TransactionsService.model.enums.JobStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "jobs", schema = "public")
public class JobsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_Id", nullable = false)
    private UsersEntity user;

    @Column(name = "unfinished_tasks", nullable = false)
    private Integer unfinishedTasks;

    @Column(name = "job_status", nullable = false)
    private JobStatus jobStatus;

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
