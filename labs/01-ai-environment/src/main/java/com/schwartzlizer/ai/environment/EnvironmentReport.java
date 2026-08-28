package com.schwartzlizer.ai.environment;

public final class EnvironmentReport {
    public String create(String runtimeName, String runtimeVersion) {
        if (runtimeName == null || runtimeName.isBlank()) {
            throw new IllegalArgumentException("Runtime name is required");
        }
        if (runtimeVersion == null || runtimeVersion.isBlank()) {
            throw new IllegalArgumentException("Runtime version is required");
        }
        return "Java AI environment ready: " + runtimeName.trim() + " " + runtimeVersion.trim();
    }
}
