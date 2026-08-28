package com.schwartzlizer.ai.chat;

import java.util.Locale;

public final class DemoCustomerSupportAiClient implements CustomerSupportAiClient {
    @Override
    public String reply(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        var normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("crash") || normalized.contains("error")) {
            return "Please restart the app, share your app version, and tell us the steps that reproduce the issue.";
        }
        if (normalized.contains("refund") || normalized.contains("charge")) {
            return "We understand the billing concern. Please share the transaction reference with our billing support team.";
        }
        return "Thanks for contacting support. Please share the relevant account-free details so we can investigate.";
    }
}
