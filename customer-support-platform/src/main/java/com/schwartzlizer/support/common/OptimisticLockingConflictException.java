package com.schwartzlizer.support.common;

public class OptimisticLockingConflictException extends RuntimeException {
    public OptimisticLockingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
