package com.schwartzlizer.ai.chat;

import java.time.Instant;
import java.util.Map;

public record ApiError(String code, String message, Instant timestamp, Map<String, String> fieldErrors) {
    public ApiError {
        fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
    }
}
