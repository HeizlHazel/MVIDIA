package com.kh.mvidia.notion.dto;
import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScheduleSummaryDto {

    // selectCompanySummaryByBp용 필드
    private String bpPartner;
    private Integer doneCount;
    private Integer defCount;
    private Integer totalCount;
    private Double progRate;

    // selectAllSchrWithProgRate용 추가 필드
    private String schrId;
    private String schNo;
    private String schName;
    private Date startDate;
    private Date endDate;
    private Integer ingCount;
    private Integer delayCount;
    private Integer waitCount;
    private Integer totalDefCount;

    // 노션용 JSON 변환 메서드
    public String toNotionJson() {
        JSONObject json = new JSONObject();
        JSONObject properties = new JSONObject();

        JSONArray bpPartnerArray = new JSONArray();
        bpPartnerArray.put(new JSONObject()
                .put("text", new JSONObject().put("content", bpPartner != null ? bpPartner : "")));
        properties.put("업체", new JSONObject().put("rich_text", bpPartnerArray));



        json.put("업체", new JSONObject().put("rich_text", new org.json.JSONArray().put(new JSONObject().put("text", new JSONObject().put("content", bpPartner)))));
        json.put("완료수", new JSONObject().put("number", doneCount));
        json.put("불량수", new JSONObject().put("number", defCount));
        json.put("전체수", new JSONObject().put("number", totalCount));
        json.put("진행률", new JSONObject().put("number", progRate));
        return json.toString();

    }
}
