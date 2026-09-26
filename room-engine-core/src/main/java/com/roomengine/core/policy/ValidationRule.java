package com.roomengine.core.policy;

public interface ValidationRule<T> {
    String name();
    ValidationResult validate(T command);
}
