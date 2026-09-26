package com.roomengine.core.policy;

public final class ValidationResult {
    private final boolean valid;
    private final String reason;

    public ValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public boolean valid() { return valid; }
    public String reason() { return reason; }
    public boolean isValid() { return valid; }
    public String getReason() { return reason; }

    public static ValidationResult accepted() { return new ValidationResult(true, ""); }
    public static ValidationResult invalid(String reason) { return new ValidationResult(false, reason); }
}
