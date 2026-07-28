package com.incidentbot.incident_processor.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Value("${app.ai.provider:claude}")
    private String provider;

    @Bean
    @Primary
    public ChatModel primaryChatModel(AnthropicChatModel anthropicModel) {
        return switch (provider.toLowerCase()) {
            case "claude" -> anthropicModel;
            default -> throw new IllegalArgumentException(
                    "Unknown provider: [" + provider + "]. Add the provider's dependency to pom.xml to use it.");
        };
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
