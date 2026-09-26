package com.roomengine.api;

import com.roomengine.core.exception.ErrorCode;
import com.roomengine.core.exception.RoomException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RoomException.class)
    public Result<Void> handleRoomException(RoomException exception) {
        return Result.failure(exception.getErrorCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException() {
        return Result.failure(ErrorCode.INVALID_ARGUMENT);
    }
}
