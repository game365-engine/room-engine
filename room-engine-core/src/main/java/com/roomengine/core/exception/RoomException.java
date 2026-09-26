package com.roomengine.core.exception;

public final class RoomException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;

    public RoomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
