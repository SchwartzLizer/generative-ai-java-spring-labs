package com.schwartzlizer.support.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security")
public record SecurityProperties(String agentUsername, String agentPassword, String adminUsername, String adminPassword) {
    public SecurityProperties { agentUsername=agentUsername == null || agentUsername.isBlank() ? "agent" : agentUsername; adminUsername=adminUsername == null || adminUsername.isBlank() ? "admin" : adminUsername; agentPassword=agentPassword == null ? "" : agentPassword; adminPassword=adminPassword == null ? "" : adminPassword; }
}
