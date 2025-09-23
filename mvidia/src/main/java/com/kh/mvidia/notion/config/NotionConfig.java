package com.kh.mvidia.notion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notion")
public class NotionConfig {
    private String apiToken;
    private String databaseId;

    @Bean(name = "notionWebClient")
    public WebClient notionWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.notion.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("Notion-Version", "2022-06-28")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
