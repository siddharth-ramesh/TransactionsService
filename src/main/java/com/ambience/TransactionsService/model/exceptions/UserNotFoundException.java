package com.ambience.TransactionsService.model.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserNotFoundException extends Exception
{
    public UserNotFoundException() {}

    public UserNotFoundException(String message) {
        super(message);
        log.info(message);
    }
}
