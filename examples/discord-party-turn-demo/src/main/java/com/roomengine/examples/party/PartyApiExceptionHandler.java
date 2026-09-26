package com.roomengine.examples.party;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.roomengine.examples.party")
public final class PartyApiExceptionHandler {
    @ExceptionHandler(PartyGameException.class)
    public PartyError handlePartyException(PartyGameException exception) {
        return new PartyError(exception.getErrorCode().name(), exception.getErrorCode().getMessage());
    }

    public record PartyError(String code, String message) { }

}
