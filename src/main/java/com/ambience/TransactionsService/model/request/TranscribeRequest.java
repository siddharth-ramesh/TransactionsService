package com.ambience.TransactionsService.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TranscribeRequest {

    @NotBlank(message = "audioChunkPaths cannot be blank")
    private List<String> audioChunkPaths;

    @NotBlank(message = "userId cannot be blank")
    private String userId;

}