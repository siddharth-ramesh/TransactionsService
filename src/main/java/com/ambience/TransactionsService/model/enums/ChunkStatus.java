package com.ambience.TransactionsService.model.enums;

public enum ChunkStatus {
    AWAITING, // chunk has not yet started processing
    PROCESSING, // chunk is currently being processed
    TRANSCRIPTION_SUCCESS, // chunk was processed and transcribed successfully
    TRANSCRIPTION_FAILURE // chunk was processed and transcription failed
}