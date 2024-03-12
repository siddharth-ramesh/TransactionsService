package com.ambience.TransactionsService.service;

import com.ambience.TransactionsService.model.entity.ChunksEntity;
import com.ambience.TransactionsService.model.entity.JobsEntity;
import com.ambience.TransactionsService.model.entity.UsersEntity;
import com.ambience.TransactionsService.model.enums.ChunkStatus;
import com.ambience.TransactionsService.model.enums.JobStatus;
import com.ambience.TransactionsService.model.exceptions.JobNotFoundException;
import com.ambience.TransactionsService.model.exceptions.UserNotFoundException;
import com.ambience.TransactionsService.model.repository.ChunksRepository;
import com.ambience.TransactionsService.model.repository.JobsRepository;
import com.ambience.TransactionsService.model.repository.UsersRepository;
import com.ambience.TransactionsService.model.response.TranscriptResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class JobConcierge {

    private UsersRepository usersRepository;
    private JobsRepository jobsRepository;
    private ChunksRepository chunksRepository;

    public JobConcierge(UsersRepository usersRepository, JobsRepository jobsRepository, ChunksRepository chunksRepository) {
        this.usersRepository = usersRepository;
        this.jobsRepository = jobsRepository;
        this.chunksRepository = chunksRepository;
    }

    public TranscriptResult lookupJob(String jobId) throws JobNotFoundException {
        Optional<JobsEntity> optionalJob = jobsRepository.findById(UUID.fromString(jobId));
        if (optionalJob.isEmpty()) throw new JobNotFoundException("jobId not found");

        JobsEntity job = optionalJob.get();
        TranscriptResult res = new TranscriptResult();
        res.setJobStatus(job.getJobStatus().name());
        if (job.getCompletedAt() != null) res.setCompletedTime(job.getCompletedAt());

        ChunksEntity c = new ChunksEntity();
        c.setJob(job);
        Example<ChunksEntity> e = Example.of(c);
        List<ChunksEntity> chunks = chunksRepository.findAll(e, Sort.by(Sort.Direction.ASC, "chunkIndex"));
        Map<String, ChunkStatus> chunkStatuses = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        for (ChunksEntity chunk : chunks) {
            chunkStatuses.put(chunk.getAudioPath(), chunk.getChunkStatus());

            if (chunk.getChunkStatus() == ChunkStatus.TRANSCRIPTION_SUCCESS) {
                sb.append(chunk.getTranscription());
            } else if (chunk.getChunkStatus() == ChunkStatus.TRANSCRIPTION_FAILURE) {
                sb.append("\n <<<"+chunk.getAudioPath()+" transcription failed>>>\n");
            } else {
                sb.append("\n <<<"+chunk.getAudioPath()+" transcription processing>>>\n");
            }
        }

        res.setChunkStatuses(chunkStatuses);
        res.setTranscriptText(sb.toString());
        return res;
    }

    public List<TranscriptResult> searchJobs(String jobStatus, String userId) throws UserNotFoundException, JobNotFoundException {
        Optional<UsersEntity> optionalUser = usersRepository.findById(UUID.fromString(userId));
        if (optionalUser.isEmpty()) throw new UserNotFoundException("userId not found");

        UsersEntity user = optionalUser.get();
        List<TranscriptResult> res = new ArrayList<>();

        JobsEntity j = new JobsEntity();
        j.setUser(user);
        if (jobStatus != null) {
            j.setJobStatus(JobStatus.valueOf(jobStatus));
        }
        Example<JobsEntity> e = Example.of(j);
        Pageable sortedByDateDesc =
                PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<JobsEntity> jobs = jobsRepository.findAll(e, sortedByDateDesc);
        List<JobsEntity> pageContent = jobs.getContent();

        for (JobsEntity je : pageContent) {
            res.add(lookupJob(String.valueOf(je.getId())));
        }

        return res;
    }
}
