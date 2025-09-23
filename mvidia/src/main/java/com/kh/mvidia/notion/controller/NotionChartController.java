package com.kh.mvidia.notion.controller;

import com.kh.mvidia.notion.service.NotionChartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotionChartController {

    private final NotionChartService notionService;

    public NotionChartController(NotionChartService notionService) {
        this.notionService = notionService;
    }

    @GetMapping("/notion/sendSummary")
    public String sendCompanySummary() {
        notionService.sendCompanySummaryToNotion();
        return "Notion 전송 완료!";
    }
}
