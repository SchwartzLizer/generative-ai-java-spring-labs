package com.schwartzlizer.support.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AiProviderConfiguration {
    @Bean
    @ConditionalOnProperty(prefix="app.ai", name="provider", havingValue="demo", matchIfMissing=true)
    CustomerSupportAiClient demoCustomerSupportAiClient() { return new DemoCustomerSupportAiClient(); }

    @Bean
    @ConditionalOnProperty(prefix="app.ai", name="provider", havingValue="gemini")
    ChatCompletionGateway geminiChatCompletionGateway(ChatClient.Builder builder, @Value("${spring.ai.google.genai.api-key:}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("GEMINI_API_KEY must be set when AI_PROVIDER=gemini");
        ChatClient chatClient=builder.build();
        return prompt -> chatClient.prompt().user(prompt).call().content();
    }

    @Bean
    @ConditionalOnProperty(prefix="app.ai", name="provider", havingValue="gemini")
    CustomerSupportAiClient geminiCustomerSupportAiClient(ChatCompletionGateway gateway, ObjectMapper objectMapper, AnalysisPromptFactory analysisPromptFactory, ResponsePromptFactory responsePromptFactory) { return new GeminiCustomerSupportAiClient(gateway, objectMapper, analysisPromptFactory, responsePromptFactory); }
}
