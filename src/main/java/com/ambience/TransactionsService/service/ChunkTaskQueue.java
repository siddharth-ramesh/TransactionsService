package com.ambience.TransactionsService.service;

import com.ambience.TransactionsService.model.ChunkTaskQueueItem;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Singleton
@Service
public class ChunkTaskQueue {

    private LinkedBlockingQueue<ChunkTaskQueueItem> chunkTaskQueue;

    public ChunkTaskQueue() {
        chunkTaskQueue = new LinkedBlockingQueue<>();
    }

    public ChunkTaskQueueItem getNextChunk() throws InterruptedException {
        return chunkTaskQueue.poll(50 ,TimeUnit.MILLISECONDS);
    }

    public void insertChunk(UUID jobId, UUID chunkId) throws InterruptedException {
        ChunkTaskQueueItem ctqi = new ChunkTaskQueueItem();
        ctqi.setJobId(jobId);
        ctqi.setChunkId(chunkId);
        ctqi.setQueueEntryTime(OffsetDateTime.now());

        chunkTaskQueue.offer(ctqi, 30 ,TimeUnit.SECONDS);
    }
}
