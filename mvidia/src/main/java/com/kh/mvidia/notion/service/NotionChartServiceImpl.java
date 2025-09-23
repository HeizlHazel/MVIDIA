package com.kh.mvidia.notion.service;

import com.kh.mvidia.notion.dto.ScheduleSummaryDto;
import com.kh.mvidia.product.model.service.ChartService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class NotionChartServiceImpl implements NotionChartService {

    private final WebClient webClient;
    private final ChartService chartService;

    @Value("${notion.api.token}")
    private String notionToken;

    @Value("${notion.database.id}")
    private String databaseId;

    // ✅ 여기서 @Qualifier 사용
    public NotionChartServiceImpl(@Qualifier("notionWebClient") WebClient webClient,
                                  ChartService chartService) {
        this.webClient = webClient;
        this.chartService = chartService;
    }

    @Override
    public void sendCompanySummaryToNotion() {
        List<ScheduleSummaryDto> list = chartService.getCompanySummary();

        for (ScheduleSummaryDto dto : list) {
            webClient.post()
                    .uri("/pages")  // baseUrl이 config에 있으므로 여기서는 상대경로
                    .header("Authorization", "Bearer " + notionToken)
                    .header("Notion-Version", "2022-06-28")
                    .header("Content-Type", "application/json")
                    .bodyValue("{\"parent\":{\"database_id\":\""+databaseId+"\"},\"properties\":"+dto.toNotionJson()+"}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
    }
}