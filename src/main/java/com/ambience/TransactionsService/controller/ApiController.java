package com.ambience.TransactionsService.controller;

import com.ambience.TransactionsService.model.exceptions.JobNotFoundException;
import com.ambience.TransactionsService.model.exceptions.UserNotFoundException;
import com.ambience.TransactionsService.model.request.TranscribeRequest;
import com.ambience.TransactionsService.model.response.TranscriptResult;
import com.ambience.TransactionsService.service.JobConcierge;
import com.ambience.TransactionsService.service.JobReceiver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final JobReceiver jobReceiver;
    private final JobConcierge jobConcierge;

    public ApiController(JobReceiver jobReceiver, JobConcierge jobConcierge) {
        this.jobReceiver = jobReceiver;
        this.jobConcierge = jobConcierge;
    }

    @GetMapping("/transcript/{jobId}")
    public ResponseEntity<TranscriptResult> getTranscript(@PathVariable String jobId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(jobConcierge.lookupJob(jobId));
        } catch(JobNotFoundException jnfe) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/transcript/search")
    public ResponseEntity<List<TranscriptResult>> getTranscriptSearch(
            @RequestParam(required = false) String jobStatus,
            @RequestParam(required = true) String userId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(jobConcierge.searchJobs(jobStatus, userId));
        } catch(UserNotFoundException unfe) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> postTranscribe(@RequestBody TranscribeRequest transcribeRequest) {
        String jobId = "";

        try {
            jobId = jobReceiver.addNewJob(transcribeRequest);
        } catch(UserNotFoundException unfe) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("userId not found");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Please try again.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(jobId);
    }
}
