package com.kh.mvidia.notion.service;

import com.kh.mvidia.notion.dto.ScheduleSummaryDto;
import com.kh.mvidia.product.model.service.ChartService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotionChartServiceImpl implements NotionChartService {

    private final WebClient webClient;
    private final ChartService cService;

    @Value("${notion.chart.database.id}")
    private String databaseId;

    public NotionChartServiceImpl(@Qualifier("notionWebClient") WebClient webClient,
                                  ChartService cService) {
        this.webClient = webClient;
        this.cService = cService;
    }

    @Override
    public void sendCompanySummaryToNotion() {
        List<ScheduleSummaryDto> list = cService.getCompanySummary();

        for (ScheduleSummaryDto dto : list) {
            String jsonBody = "{\"parent\":{\"database_id\":\"" + databaseId + "\"}," +
                    "\"properties\":" + dto.toNotionJson() + "}";

            webClient.post()
                    .uri("/pages")
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
    }

    private void createNotionPageWithTables(List<ScheduleSummaryDto> companySummary,
                                            List<ScheduleSummaryDto> scheduleDetails) {
        Map<String, Object> pageData = new HashMap<>();

        // 페이지 생성 요청 본문 구성
        Map<String, Object> parent = new HashMap<>();
        parent.put("database_id", databaseId);
        pageData.put("parent", parent);

        // 프로퍼티 설정 (제목 등)
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> title = new HashMap<>();
        title.put("title", List.of(Map.of("text", Map.of("content", "품질 검사 보고서"))));
        properties.put("Name", title);  // Notion 데이터베이스의 제목 필드명에 맞게 조정 필요
        pageData.put("properties", properties);

        // children 블록에 2개의 테이블 추가
        // 실제 구현은 Notion API 문서 참고하여 테이블 블록 생성

        webClient.post()
                .uri("/pages")
                .bodyValue(pageData)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}