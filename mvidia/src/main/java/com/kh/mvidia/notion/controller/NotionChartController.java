package com.kh.mvidia.notion.controller;

import com.kh.mvidia.notion.service.NotionChartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class NotionChartController {

    private final NotionChartService notionService;

    public NotionChartController(NotionChartService notionService) {
        this.notionService = notionService;
    }

    @GetMapping("/notion/sendSummary")
    @ResponseBody
    public Map<String, String> sendCompanySummary() {
        Map<String, String> response = new HashMap<>();
        try {
            notionService.sendCompanySummaryToNotion();
            response.put("status", "success");
            response.put("message", "Notion 전송 완료!");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "전송 실패: " + e.getMessage());
        }
        return response;
    }
}
