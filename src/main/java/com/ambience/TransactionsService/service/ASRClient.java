package com.ambience.TransactionsService.service;

import com.ambience.TransactionsService.model.ChunkTaskQueueItem;
import com.ambience.TransactionsService.model.entity.ChunksEntity;
import com.ambience.TransactionsService.model.entity.JobsEntity;
import com.ambience.TransactionsService.model.enums.ChunkStatus;
import com.ambience.TransactionsService.model.repository.ChunksRepository;
import com.ambience.TransactionsService.model.repository.JobsRepository;
import com.ambience.TransactionsService.model.response.GetAsrOutputResponse;
import com.ambience.TransactionsService.utils.Utils;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ASRClient {
    private ChunkTaskQueue chunkTaskQueue;
    private ChunksRepository chunksRepository;
    private JobsRepository jobsRepository;
    private Utils utils;

    public ASRClient(ChunkTaskQueue chunkTaskQueue, ChunksRepository chunksRepository, JobsRepository jobsRepository, Utils utils) {
        this.chunkTaskQueue = chunkTaskQueue;
        this.chunksRepository = chunksRepository;
        this.jobsRepository = jobsRepository;
        this.utils = utils;
    }

    @Async
    public void processChunksAsynchronously() {
        while(true){
            try {
                ChunkTaskQueueItem ctqi = chunkTaskQueue.getNextChunk();
                if (ctqi != null) {
                    log.info("Picked up queue item " + ctqi.getChunkId());

                    Optional<ChunksEntity> optionalResult = chunksRepository.findById(ctqi.getChunkId());
                    if (optionalResult.isEmpty()) continue;

                    ChunksEntity chunk = optionalResult.get();
                    chunk.setChunkStatus(ChunkStatus.PROCESSING);
                    chunk = chunksRepository.save(chunk);
                    String url = "http://localhost:3000/get-asr-output?path=" + chunk.getAudioPath();
                    Future<Response> response = null;

                    for (int i = 3; i > 0; i--) {
                        // retry as many times as configured if call fails
                        response = getAsyncHttp(url);
                        while (!response.isDone()) {
                            Thread.sleep(100);
                        }

                        if (response.get().getStatus() == 200) break;
                    }

                    if (response.get().getStatus() == 200) {
                        GetAsrOutputResponse asrOutput = utils.getAsrOutputFromResponse(response.get().readEntity(String.class));
                        chunk.setTranscription(asrOutput.getTranscript());
                        chunk.setCompletedAt(OffsetDateTime.now());
                        chunk.setChunkStatus(ChunkStatus.TRANSCRIPTION_SUCCESS);
                    } else {
                        chunk.setCompletedAt(OffsetDateTime.now());
                        chunk.setChunkStatus(ChunkStatus.TRANSCRIPTION_FAILURE);
                    }

                    chunksRepository.save(chunk);
                    setJobsRepositoryTasks(ctqi.getJobId());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {}
        }
    }

    public synchronized void setJobsRepositoryTasks(UUID id) {
        log.info("Decrementing job id: " + String.valueOf(id));
        JobsEntity job = jobsRepository.findById(id).get();
        int unfinishedTasks = job.getUnfinishedTasks();

        if (unfinishedTasks == 1) { // this chunk will complete the job
            utils.markJobAsCompleted(job);
        } else {
            job.setUnfinishedTasks(unfinishedTasks-1);
            jobsRepository.saveAndFlush(job);
        }
    }

    public Future<Response> getAsyncHttp(final String url) {
        Client client = ClientBuilder.newBuilder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(10100, TimeUnit.MILLISECONDS)
                .build();
        return client.target(url).request().async().get();
    }
}
