package com.ambience.TransactionsService.model.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobNotFoundException extends Exception
{
    public JobNotFoundException() {}

    public JobNotFoundException(String message) {
        super(message);
        log.info(message);
    }
}
