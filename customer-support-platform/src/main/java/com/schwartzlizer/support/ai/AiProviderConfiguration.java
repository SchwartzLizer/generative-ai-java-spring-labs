package com.schwartzlizer.support.ai;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiProviderConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "demo", matchIfMissing = true)
    CustomerSupportAiClient demoCustomerSupportAiClient() {
        return new DemoCustomerSupportAiClient();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "gemini")
    Client geminiClient(AiProviderProperties properties,
                        @Value("${spring.ai.google.genai.api-key:}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY must be set when AI_PROVIDER=gemini");
        }
        long millis = properties.timeout().toMillis();
        if (millis > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("AI timeout is too large");
        }
        return Client.builder()
            .apiKey(apiKey)
            .httpOptions(HttpOptions.builder().timeout((int) millis).build())
            .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "gemini")
    ChatCompletionGateway geminiChatCompletionGateway(ChatClient.Builder builder) {
        ChatClient chatClient = builder.build();
        return prompt -> chatClient.prompt().user(prompt).call().content();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "gemini")
    CustomerSupportAiClient geminiCustomerSupportAiClient(
        ChatCompletionGateway gateway,
        com.fasterxml.jackson.databind.ObjectMapper objectMapper,
        AnalysisPromptFactory analysisPromptFactory,
        ResponsePromptFactory responsePromptFactory) {
        return new GeminiCustomerSupportAiClient(gateway, objectMapper, analysisPromptFactory, responsePromptFactory);
    }
}
