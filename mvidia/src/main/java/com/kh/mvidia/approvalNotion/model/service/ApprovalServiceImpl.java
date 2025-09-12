package com.kh.mvidia.approvalNotion.model.service;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    @Override
    public HttpResponse<JsonNode> addApproval(String title, String category, String writer) {

        String url = "https://api.notion.com/v1/pages";

        JSONObject parent = new JSONObject();
        parent.put("database_id", database_id);

        JSONObject properties = new JSONObject();

        JSONArray titleArray = new JSONArray(); // []
        JSONObject titleContent = new JSONObject(); // {}
        titleContent.put("text", new JSONObject().put("content", title));
        titleArray.add(titleContent); // "title":[{"text":{"content": ..}}]
        properties.put("제목", new JSONObject().put("title", titleArray));

        // 구분(select 속성)
        JSONObject selectObject = new JSONObject(); // {}
        selectObject.put("name", category);
        properties.put("구분", new JSONObject().put("select", selectObject));

        // 작성자(text 속성)
        // page.작성자.rich_text[0].text.content
        JSONArray writerArray = new JSONArray(); // []
        JSONObject writerContent = new JSONObject(); // {}
        writerContent.put("text", new JSONObject().put("content", writer));
        writerArray.add(writerContent);
        properties.put("작성자", new JSONObject().put("rich_text", writerArray));

        // 작성일(현재 날짜)
        // page.작성일.date.start
        String dateOnly = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        JSONObject dateObject = new JSONObject();
        dateObject.put("start", dateOnly);
        properties.put("작성일", new JSONObject().put("date", dateObject));

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
