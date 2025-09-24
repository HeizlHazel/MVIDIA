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

    @Value("${notion.chart.summary.id}")
    private String summaryDatabaseId;  // 업체별 요약 테이블

    @Value("${notion.chart.detail.id}")
    private String detailDatabaseId;   // 일정 상세 테이블

    public NotionChartServiceImpl(@Qualifier("notionWebClient") WebClient webClient,
                                  ChartService cService) {
        this.webClient = webClient;
        this.cService = cService;
    }

    @Override
    public void sendCompanySummaryToNotion() {
        // 1. 업체별 요약 데이터 전송
        List<ScheduleSummaryDto> companySummary = cService.getCompanySummary();
        for (ScheduleSummaryDto dto : companySummary) {
            String jsonBody = "{\"parent\":{\"database_id\":\"" + summaryDatabaseId + "\"}," +
                    "\"properties\":" + dto.toCompanySummaryJson() + "}";

            webClient.post()
                    .uri("/pages")
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }

        // 2. 일정 상세 데이터 전송
        List<ScheduleSummaryDto> scheduleDetails = cService.getScheduleDetail();
        for (ScheduleSummaryDto dto : scheduleDetails) {
            String jsonBody = "{\"parent\":{\"database_id\":\"" + detailDatabaseId + "\"}," +
                    "\"properties\":" + dto.toScheduleDetailJson() + "}";

            webClient.post()
                    .uri("/pages")
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
    }
}