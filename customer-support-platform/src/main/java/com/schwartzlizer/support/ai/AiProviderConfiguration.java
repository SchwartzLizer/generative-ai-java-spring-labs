package com.schwartzlizer.support.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import okhttp3.OkHttpClient;
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
    Client geminiClient(
        @Value("${spring.ai.google.genai.api-key:}") String apiKey,
        AiProviderProperties provider
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY must be set when AI_PROVIDER=gemini");
        }
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(provider.timeout())
            .readTimeout(provider.timeout())
            .writeTimeout(provider.timeout())
            .callTimeout(provider.timeout())
            .build();
        ClientOptions clientOptions = ClientOptions.builder()
            .customHttpClient(httpClient)
            .build();
        return Client.builder()
            .apiKey(apiKey)
            .clientOptions(clientOptions)
            .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "gemini")
    ChatCompletionGateway geminiChatCompletionGateway(ChatClient.Builder builder) {
        ChatClient chatClient = builder.build();
        return prompt -> {
            try {
                return chatClient.prompt().user(prompt).call().content();
            } catch (RuntimeException exception) {
                throw new com.schwartzlizer.support.common.AiProviderException(
                    "AI provider request failed",
                    exception
                );
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "gemini")
    CustomerSupportAiClient geminiCustomerSupportAiClient(
        ChatCompletionGateway gateway,
        ObjectMapper objectMapper,
        AnalysisPromptFactory analysisPromptFactory,
        ResponsePromptFactory responsePromptFactory
    ) {
        return new GeminiCustomerSupportAiClient(
            gateway,
            objectMapper,
            analysisPromptFactory,
            responsePromptFactory
        );
    }
}
