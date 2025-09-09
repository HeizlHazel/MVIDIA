package com.kh.mvidia.notion.service;

import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import com.kh.mvidia.notion.config.NotionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotionServiceImpl implements NotionService {

    private final NotionConfig config;
    private static final String NOTION_URL = "https://api.notion.com/v1/pages";

    @Autowired
    public NotionServiceImpl(NotionConfig config) {
        this.config = config;
    }

    @Override
    public void insertPayrollToNotion(Salary salary, List<Tax> taxList) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiToken());
        headers.set("Notion-Version", "2022-06-28");

        Map<String , Object> body = new HashMap<>();
        body.put("parent", Map.of("database_id", config.getDatabaseId()));

        Map<String , Object> properties = new HashMap<>();
        properties.put("명세서 이름", Map.of("title", List.of(
                Map.of("text", Map.of("content", salary.getPayDate() + " " + salary.getEmpNo() + " 급여명세서"))
        )));
        properties.put("사번", Map.of("rich_text", List.of(
                Map.of("text", Map.of("content", salary.getEmpNo()))
        )));
        properties.put("지급월", Map.of(
                "date", Map.of("start", salary.getPayDate())
        ));
        properties.put("기본급", Map.of("number", Integer.parseInt(salary.getSalary())));
        properties.put("연장근무수당", Map.of("number", Integer.parseInt(salary.getExtendOv())));
        properties.put("야간수당", Map.of("number", Integer.parseInt(salary.getNightOv())));
        properties.put("휴일근무수당", Map.of("number", Integer.parseInt(salary.getWeekendOv())));
        properties.put("출장수당", Map.of("number", Integer.parseInt(salary.getTripOv())));
        properties.put("보너스", Map.of("number", Integer.parseInt(salary.getBonus())));

        if (taxList != null && !taxList.isEmpty()) {
            taxList.forEach(tax -> {
                int amount = parseIntSafe(tax.getAmount());

                switch (tax.getTaxCode()) {
                    case "TAX0001": properties.put("국민연금", Map.of("number", amount)); break;
                    case "TAX0002": properties.put("건강보험", Map.of("number", amount)); break;
                    case "TAX0003": properties.put("고용보험", Map.of("number", amount)); break;
                    case "TAX0004": properties.put("소득세", Map.of("number", amount)); break;
                    case "TAX0005": properties.put("지방소득세", Map.of("number", amount)); break;
                }
            });
        }

        body.put("properties", properties);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(NOTION_URL, request, String.class);

        System.out.println("Notion 응답: " + response.getBody());
    }

    private int parseIntSafe(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

