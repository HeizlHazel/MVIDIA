package com.kh.mvidia.notion.service;

import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NotionServiceImpl implements NotionService {

    @Value("${notion.api-token}")
    private String notionToken;

    @Value("${notion.database-id}")
    private String databaseId;

    private static final String NOTION_URL = "https://api.notion.com/v1/pages";

    @Override
    public void insertPayrollToNotion(Salary salary, List<Tax> taxList) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + notionToken);
            headers.set("Notion-Version", "2022-06-28");
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            JSONObject parent = new JSONObject();
            parent.put("database_id", databaseId);
            body.put("parent", parent);

            JSONObject properties = new JSONObject();

            // 스크린샷에서 보이는 정확한 속성명 사용

            // 명세서 이름 (title) - 스크린샷에서 첫 번째 컬럼명 사용
            JSONArray titleArray = new JSONArray();
            titleArray.put(new JSONObject().put("text", new JSONObject()
                    .put("content", salary.getEmpNo() + "_" + salary.getPayDate() + " 급여명세서")));
            properties.put("속성명", new JSONObject().put("title", titleArray)); // 첫 번째 타이틀 컬럼

            // 구분, 사번, 이름 등 - 실제 데이터베이스에 있는 속성명만 사용
            // 먼저 몇 개 속성만 테스트해보세요

            // 구분 - Select 타입으로 수정
            properties.put("구분", new JSONObject().put("select", new JSONObject().put("name", "급여")));

            // 다른 속성들은 실제 데이터베이스에 존재하는지 확인 후 추가
            // properties.put("사번", new JSONObject().put("rich_text", ...));
            // properties.put("이름", new JSONObject().put("rich_text", ...));

            body.put("properties", properties);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
            String response = restTemplate.postForObject(NOTION_URL, request, String.class);

            System.out.println("▶ Notion 응답: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === Helper Methods ===
    private int getTaxAmount(List<Tax> taxList, String taxCode) {
        return taxList.stream()
                .filter(t -> taxCode.equals(t.getTaxCode()))
                .mapToInt(t -> parseInt(t.getAmount()))
                .sum();
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}