package com.ambience.TransactionsService.model.response;

import com.ambience.TransactionsService.model.enums.ChunkStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
public class TranscriptResult {

    String transcriptText;
    Map<String, ChunkStatus> chunkStatuses;
    String JobStatus;
    OffsetDateTime completedTime;
}
