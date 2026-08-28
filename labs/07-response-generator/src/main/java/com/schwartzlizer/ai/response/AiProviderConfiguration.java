package com.schwartzlizer.ai.response;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiProviderConfiguration {
    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "demo", matchIfMissing = true)
    SupportResponseAiClient demoSupportResponseAiClient() {
        return new DemoSupportResponseAiClient();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
    ChatCompletionGateway geminiChatCompletionGateway(ChatClient.Builder builder) {
        var chatClient = builder.build();
        return prompt -> chatClient.prompt().user(prompt).call().content();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
    SupportResponseAiClient geminiSupportResponseAiClient(ChatCompletionGateway gateway) {
        return new GeminiSupportResponseAiClient(gateway);
    }
}
