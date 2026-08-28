package com.schwartzlizer.ai.environment;

public final class EnvironmentApplication {
    private EnvironmentApplication() {
    }

    public static void main(String[] args) {
        var report = new EnvironmentReport().create(
                System.getProperty("java.runtime.name"),
                System.getProperty("java.runtime.version"));
        System.out.println(report);
    }
}
