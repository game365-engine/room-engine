package com.roomengine.examples.party;

import java.util.Locale;

public enum PartyAction {
    ROLL,
    PASS;

    public static PartyAction parse(String value) {
        if (value == null || value.isBlank()) {
            throw new PartyGameException(PartyErrorCode.INVALID_ACTION);
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new PartyGameException(PartyErrorCode.INVALID_ACTION);
        }
    }
}
