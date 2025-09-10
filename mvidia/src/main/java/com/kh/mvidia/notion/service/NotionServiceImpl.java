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

    @Value("${notion.api-token}")   // application.properties 맞춤
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

            // === JSON Body ===
            JSONObject body = new JSONObject();
            JSONObject parent = new JSONObject();
            parent.put("database_id", databaseId);
            body.put("parent", parent);

            JSONObject properties = new JSONObject();

            // 명세서 이름 (title 타입)
            JSONObject titleObject = new JSONObject();
            JSONObject titleText = new JSONObject();
            titleText.put("content", String.format("%s_%s 급여명세서", salary.getEmpNo(), salary.getPayDate()));
            titleObject.put("text", titleText);
            JSONArray titleArray = new JSONArray();
            titleArray.put(titleObject);
            properties.put("명세서 이름", new JSONObject().put("title", titleArray));

            // 사번 (rich_text 타입)
            JSONObject richTextObject = new JSONObject();
            JSONObject richTextContent = new JSONObject();
            richTextContent.put("content", salary.getEmpNo());
            richTextObject.put("text", richTextContent);
            JSONArray richTextArray = new JSONArray();
            richTextArray.put(richTextObject);
            properties.put("사번", new JSONObject().put("rich_text", richTextArray));

            // 이름 (rich_text 타입)
            JSONObject richTextNameObject = new JSONObject();
            JSONObject richTextNameContent = new JSONObject();
            richTextNameContent.put("content", salary.getEmpName());
            richTextNameObject.put("text", richTextNameContent);
            JSONArray richTextNameArray = new JSONArray();
            richTextNameArray.put(richTextNameObject);
            properties.put("이름", new JSONObject().put("rich_text", richTextNameArray));

            // 부서 (select 타입)
            JSONObject selectDept = new JSONObject();
            selectDept.put("name", salary.getDeptName());
            properties.put("부서", new JSONObject().put("select", selectDept));

            // 직위 (select 타입)
            JSONObject selectJob = new JSONObject();
            selectJob.put("name", salary.getJobName());
            properties.put("직위", new JSONObject().put("select", selectJob));

            // 지급월 (date 타입)
            JSONObject dateObject = new JSONObject();
            dateObject.put("start", salary.getPayDate() + "-01");
            properties.put("지급월", new JSONObject().put("date", dateObject));

            // number 타입 속성들
            properties.put("기본급", new JSONObject().put("number", parseInt(salary.getSalary())));
            properties.put("연장근무수당", new JSONObject().put("number", parseInt(salary.getExtendOv())));
            properties.put("야간수당", new JSONObject().put("number", parseInt(salary.getNightOv())));
            properties.put("휴일근무수당", new JSONObject().put("number", parseInt(salary.getWeekendOv())));
            properties.put("출장수당", new JSONObject().put("number", parseInt(salary.getTripOv())));
            properties.put("보너스", new JSONObject().put("number", parseInt(salary.getBonusAmt())));

            // 세금 항목
            properties.put("국민연금", new JSONObject().put("number", getTaxAmount(taxList, "TAX001")));
            properties.put("건강보험", new JSONObject().put("number", getTaxAmount(taxList, "TAX002")));
            properties.put("고용보험", new JSONObject().put("number", getTaxAmount(taxList, "TAX003")));
            properties.put("소득세", new JSONObject().put("number", getTaxAmount(taxList, "TAX004")));
            properties.put("지방소득세", new JSONObject().put("number", getTaxAmount(taxList, "TAX005")));

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