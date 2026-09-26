package com.roomengine.examples.party;

public final class PartyGameException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final PartyErrorCode errorCode;

    public PartyGameException(PartyErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PartyErrorCode getErrorCode() {
        return errorCode;
    }
}
