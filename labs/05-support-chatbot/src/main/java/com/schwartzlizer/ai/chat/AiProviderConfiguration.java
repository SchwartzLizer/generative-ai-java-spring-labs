package com.schwartzlizer.ai.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiProviderConfiguration {
    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "demo", matchIfMissing = true)
    CustomerSupportAiClient demoCustomerSupportAiClient() {
        return new DemoCustomerSupportAiClient();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
    ChatCompletionGateway geminiChatCompletionGateway(ChatClient.Builder builder) {
        var chatClient = builder.build();
        return prompt -> chatClient.prompt().user(prompt).call().content();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
    CustomerSupportAiClient geminiCustomerSupportAiClient(ChatCompletionGateway gateway) {
        return new GeminiCustomerSupportAiClient(gateway);
    }
}
