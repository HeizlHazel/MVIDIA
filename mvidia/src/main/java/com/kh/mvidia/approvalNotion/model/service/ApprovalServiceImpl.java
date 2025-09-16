package com.kh.mvidia.approvalNotion.model.service;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Value("${notion.token}")
    private String token;

    @Value("${notion.database.id}")
    private String database_id;

    @Override
    public String getDatabase() {

        String url = "https://api.notion.com/v1/databases/" + database_id + "/query";

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", "Bearer " + token)
                .header("Notion-Version", "2022-06-28")
                .header("Content-Type", "application/json")
                .asJson(); // 요청을 보냄

        return response.getBody().toPrettyString();
    }

    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "expense", "지출결의",
            "purchase", "구매요청",
            "overtime", "초과근무",
            "access", "권한신청",
            "etc", "기타결재"
    );

    @Override
    public HttpResponse<JsonNode> addPage(String writer, String dept, String date, String title, String approval, String details, String category) {
        String categoryMap = CATEGORY_MAP.getOrDefault(category, "기타결재");

        String url = "https://api.notion.com/v1/pages";

        JSONObject parent = new JSONObject();
        parent.put("database_id", database_id);

        Map<String, Object> properties = new HashMap<>();

        // 제목
        properties.put("제목", Map.of("title",
                List.of(Map.of("text", Map.of("content", title)))));

        // 구분(select 속성)
        properties.put("구분", Map.of("select", Map.of("name", categoryMap)));

        // 작성자(text 속성)
        properties.put("작성자", Map.of("rich_text",
                List.of(Map.of("text", Map.of("content", writer)))));

        // 부서 정보(text 속성)
        properties.put("부서", Map.of("rich_text",
                List.of(Map.of("text", Map.of("content", dept)))));

        // 작성일(현재 날짜)
        properties.put("작성일", Map.of("date", Map.of("start", date)));

        // 상세 내용(text 속성)
        properties.put("내용", Map.of("rich_text",
                List.of(Map.of("text", Map.of("content", details)))));

        // 결재자 (Multi-select 타입)
        String[] approverArray = approval.split(",");
        List<Map<String, String>> approverOptions = Arrays.stream(approverArray)
                .map(String::trim)
                .map(name -> Map.of("name", name))
                .collect(Collectors.toList());
        properties.put("결재자", Map.of("multi_select", approverOptions));

        // 3. 최종 body
        JSONObject body = new JSONObject(); // {}
        body.put("parent", parent);
        body.put("properties", properties);

        // 4. HTTP 요청
        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", "Bearer " + token)
                .header("Notion-Version", "2022-06-28")
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();

        return response;
    }
}
