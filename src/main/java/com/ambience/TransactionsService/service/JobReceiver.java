package com.ambience.TransactionsService.service;

import com.ambience.TransactionsService.model.entity.ChunksEntity;
import com.ambience.TransactionsService.model.entity.JobsEntity;
import com.ambience.TransactionsService.model.entity.UsersEntity;
import com.ambience.TransactionsService.model.enums.ChunkStatus;
import com.ambience.TransactionsService.model.enums.JobStatus;
import com.ambience.TransactionsService.model.exceptions.UserNotFoundException;
import com.ambience.TransactionsService.model.repository.ChunksRepository;
import com.ambience.TransactionsService.model.repository.JobsRepository;
import com.ambience.TransactionsService.model.repository.UsersRepository;
import com.ambience.TransactionsService.model.request.TranscribeRequest;
import com.ambience.TransactionsService.utils.Utils;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobReceiver {

    private UsersRepository usersRepository;
    private JobsRepository jobsRepository;
    private ChunksRepository chunksRepository;
    private ChunkTaskQueue chunkTaskQueue;
    private Utils utils;
    private ASRClient asrClient;
    MessageDigest messageDigest;

    public JobReceiver(UsersRepository usersRepository, JobsRepository jobsRepository, ChunksRepository chunksRepository, ChunkTaskQueue chunkTaskQueue, Utils utils, ASRClient asrClient) throws NoSuchAlgorithmException {
        this.usersRepository = usersRepository;
        this.jobsRepository = jobsRepository;
        this.chunksRepository = chunksRepository;
        this.chunkTaskQueue = chunkTaskQueue;
        this.utils = utils;
        this.asrClient = asrClient;
        this.messageDigest = MessageDigest.getInstance("SHA-256");
    }

    public String addNewJob(TranscribeRequest transcribeRequest) throws UserNotFoundException, InterruptedException {
        String userId = transcribeRequest.getUserId();
        List<String> audioChunkPaths = transcribeRequest.getAudioChunkPaths();
        int totalTasks = audioChunkPaths.size();

        Optional<UsersEntity> optionalUser = usersRepository.findById(UUID.fromString(userId));
        if (optionalUser.isEmpty()) throw new UserNotFoundException("userId " + userId + " was not found");

        UsersEntity user = optionalUser.get();
        JobsEntity job = new JobsEntity();
        job.setJobStatus(JobStatus.PROCESSING);
        job.setUser(user);
        job.setUnfinishedTasks(totalTasks);
        job = jobsRepository.save(job);
        int numUnfinishedTasks = totalTasks;

        for (int i = 0; i < audioChunkPaths.size(); i++) {
            String audioChunkPath = audioChunkPaths.get(i);
            // check if hash of audioPath already exists. If so we don't need to transcribe again
            String audioChunkPathHash = getHash(audioChunkPath);
            ChunksEntity identicalChunk = findChunkByHash(audioChunkPathHash);

            ChunksEntity chunk = new ChunksEntity();
            chunk.setJob(job);
            chunk.setChunkStatus(ChunkStatus.AWAITING);
            chunk.setAudioPath(audioChunkPath);
            chunk.setHash(audioChunkPathHash);
            chunk.setChunkIndex(i);

            if (identicalChunk != null) {
                chunk.setTranscription(identicalChunk.getTranscription());
                chunk.setChunkStatus(ChunkStatus.TRANSCRIPTION_SUCCESS);
                chunk.setCompletedAt(OffsetDateTime.now());
                chunksRepository.saveAndFlush(chunk);
                totalTasks--;
                continue;
                // don't add into queue to process since we have already processed an identical chunk
            }

            chunk = chunksRepository.saveAndFlush(chunk);

            chunkTaskQueue.insertChunk(job.getId(), chunk.getId());
            asrClient.processChunksAsynchronously();
        }

        if (totalTasks == 0) {
            utils.markJobAsCompleted(job);
        } else {
            job.setUnfinishedTasks(totalTasks);
            job = jobsRepository.saveAndFlush(job);
        }

        return String.valueOf(job.getId());
    }

    private ChunksEntity findChunkByHash(String hash) {
        ChunksEntity c = new ChunksEntity();
        c.setHash(hash);
        Example<ChunksEntity> e = Example.of(c);
        List<ChunksEntity> results = chunksRepository.findAll(e);
        for (ChunksEntity result : results) {
            if (result.getTranscription() != null) return result;
        }
        return null;
    }

    private String getHash(String audioPath) {
        messageDigest.update(audioPath.getBytes());
        return new String(Base64.getEncoder().encode(messageDigest.digest()));
    }
}
