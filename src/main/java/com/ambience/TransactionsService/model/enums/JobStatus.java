package com.ambience.TransactionsService.model.enums;

public enum JobStatus {
    PROCESSING, // Some or all Chunks are still being processed
    COMPLETED, // All chunks were processed and transcribed successfully
    COMPLETED_PARTIAL, // All chunks were processed but some chunks failed to transcribe
    FAILED // All chunks were processed but all chunks failed to transcribe
}