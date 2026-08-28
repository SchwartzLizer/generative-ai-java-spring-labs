package com.schwartzlizer.support.common;

import java.time.Instant;
import java.util.Map;

public record ApiError(String code, String message, Instant timestamp, String path, Map<String, String> fieldErrors) {
    public ApiError { fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors); }
}
