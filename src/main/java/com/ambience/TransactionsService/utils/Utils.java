package com.ambience.TransactionsService.utils;

import com.ambience.TransactionsService.model.entity.ChunksEntity;
import com.ambience.TransactionsService.model.entity.JobsEntity;
import com.ambience.TransactionsService.model.enums.ChunkStatus;
import com.ambience.TransactionsService.model.enums.JobStatus;
import com.ambience.TransactionsService.model.repository.ChunksRepository;
import com.ambience.TransactionsService.model.repository.JobsRepository;
import com.ambience.TransactionsService.model.response.GetAsrOutputResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.inject.Singleton;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Singleton
@Component
public class Utils {
    final Gson gson;
    JobsRepository jobsRepository;
    ChunksRepository chunksRepository;

    public Utils(JobsRepository jobsRepository, ChunksRepository chunksRepository) {
        this.jobsRepository = jobsRepository;
        this.chunksRepository = chunksRepository;
        gson = new GsonBuilder().create();
    }

    public GetAsrOutputResponse getAsrOutputFromResponse(String response) {
        return gson.fromJson(response, GetAsrOutputResponse.class);
    }

    public synchronized void markJobAsCompleted(JobsEntity jobsEntity) {
        ChunksEntity c = new ChunksEntity();
        c.setJob(jobsEntity);
        Example<ChunksEntity> e = Example.of(c);
        List<ChunksEntity> results = chunksRepository.findAll(e);

        int passedJobs = 0;
        int failedJobs = 0;

        for (ChunksEntity result : results) {
            if (result.getChunkStatus() == ChunkStatus.AWAITING ||
            result.getChunkStatus() == ChunkStatus.PROCESSING) return;

            if (result.getChunkStatus() == ChunkStatus.TRANSCRIPTION_SUCCESS) passedJobs++;
            if (result.getChunkStatus() == ChunkStatus.TRANSCRIPTION_FAILURE) failedJobs++;
        }

        JobStatus jobStatus = JobStatus.COMPLETED_PARTIAL;
        if (passedJobs == 0) jobStatus = JobStatus.FAILED;
        if (failedJobs == 0) jobStatus = JobStatus.COMPLETED;
        jobsEntity.setJobStatus(jobStatus);
        jobsEntity.setCompletedAt(OffsetDateTime.now());
        jobsEntity.setUnfinishedTasks(0);
        jobsRepository.saveAndFlush(jobsEntity);
    }
}
