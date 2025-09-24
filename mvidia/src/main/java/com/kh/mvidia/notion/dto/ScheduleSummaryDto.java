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

    // 업체별 요약 테이블용
    public String toCompanySummaryJson() {
        JSONObject properties = new JSONObject();

        // 텍스트 필드
        properties.put("업체", createTextProperty(bpPartner));

        // 숫자 필드들
        properties.put("완료수", createNumberProperty(doneCount));
        properties.put("불량수", createNumberProperty(defCount));
        properties.put("전체수", createNumberProperty(totalCount));
        properties.put("진행률", createNumberProperty(progRate));

        return properties.toString();
    }

    // 일정 상세 테이블용
    public String toScheduleDetailJson() {
        JSONObject properties = new JSONObject();

        // 텍스트 필드
        properties.put("업체", createTextProperty(bpPartner));
        properties.put("일정명", createTextProperty(schName));

        // 날짜 필드
        properties.put("시작일", createDateProperty(startDate));
        properties.put("종료일", createDateProperty(endDate));

        // 숫자 필드들
        properties.put("완료수", createNumberProperty(doneCount));
        properties.put("진행중", createNumberProperty(ingCount));
        properties.put("지연", createNumberProperty(delayCount));
        properties.put("대기", createNumberProperty(waitCount));
        properties.put("불량수", createNumberProperty(totalDefCount));
        properties.put("진행률", createNumberProperty(progRate));

        return properties.toString();
    }

    // 헬퍼 메서드들
    private JSONObject createTextProperty(String value) {
        JSONArray array = new JSONArray();
        array.put(new JSONObject()
                .put("text", new JSONObject().put("content", value != null ? value : "")));
        return new JSONObject().put("rich_text", array);
    }

    private JSONObject createNumberProperty(Number value) {
        return new JSONObject().put("number", value != null ? value : 0);
    }

    private JSONObject createDateProperty(Date date) {
        if (date == null) {
            return new JSONObject().put("date", JSONObject.NULL);
        }
        return new JSONObject().put("date", new JSONObject().put("start", date.toString()));
    }
}
