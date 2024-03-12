package com.ambience.TransactionsService.model;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ChunkTaskQueueItem {
    UUID chunkId;
    UUID jobId;
    OffsetDateTime queueEntryTime;
}
