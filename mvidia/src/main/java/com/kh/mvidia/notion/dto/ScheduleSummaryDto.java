package com.kh.mvidia.notion.dto;
import org.json.JSONObject;

public class ScheduleSummaryDto {

    private String bpPartner;
    private int doneCount;
    private int defCount;
    private int totalCount;
    private double progRate;

    public ScheduleSummaryDto(String bpPartner, int doneCount, int defCount, int totalCount, double progRate) {
        this.bpPartner = bpPartner;
        this.doneCount = doneCount;
        this.defCount = defCount;
        this.totalCount = totalCount;
        this.progRate = progRate;
    }

    // getter / setter 생략

    // 노션용 JSON 변환 메서드
    public String toNotionJson() {
        JSONObject json = new JSONObject();
        json.put("업체", new JSONObject().put("rich_text", new org.json.JSONArray().put(new JSONObject().put("text", new JSONObject().put("content", bpPartner)))));
        json.put("완료수", new JSONObject().put("number", doneCount));
        json.put("불량수", new JSONObject().put("number", defCount));
        json.put("전체수", new JSONObject().put("number", totalCount));
        json.put("진행률", new JSONObject().put("number", progRate));
        return json.toString();
    }
}
