package com.ambience.TransactionsService.service;

import com.ambience.TransactionsService.model.entity.ChunksEntity;
import com.ambience.TransactionsService.model.enums.ChunkStatus;
import com.ambience.TransactionsService.model.repository.ChunksRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
public class JobReclaimer {

    private ChunksRepository chunksRepository;
    private ChunkTaskQueue chunkTaskQueue;

    public JobReclaimer(ChunksRepository chunksRepository, ChunkTaskQueue chunkTaskQueue) {
        this.chunksRepository = chunksRepository;
        this.chunkTaskQueue = chunkTaskQueue;
    }

    @Scheduled(fixedRate = 30000) // runs every 30 sec
    private void reclaimLostJobs() throws InterruptedException {
        log.info("Attempting to reclaim any lost jobs at: " + System.currentTimeMillis());
        List<ChunksEntity> lostChunks = chunksRepository.findLostChunks(OffsetDateTime.now().minusSeconds(45));

        for (ChunksEntity lostChunk : lostChunks) {
            lostChunk.setChunkStatus(ChunkStatus.AWAITING);
            chunksRepository.save(lostChunk);
            chunkTaskQueue.insertChunk(lostChunk.getJob().getId(), lostChunk.getId());
        }

        log.info("Reclaimed " + lostChunks.size() + " chunks at: " + System.currentTimeMillis());
    }
}
