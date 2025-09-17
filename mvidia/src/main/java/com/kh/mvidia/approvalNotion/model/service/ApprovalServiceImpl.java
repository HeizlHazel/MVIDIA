package com.kh.mvidia.approvalNotion.model.service;

import com.kh.mvidia.approvalNotion.model.dto.ApprovalDetail;
import com.kh.mvidia.approvalNotion.model.dto.ApprovalItem;
import com.kh.mvidia.approvalNotion.model.dto.NotionPageResult;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Value("${notion.token}")
    private String token;

    @Value("${notion.database.id}")
    private String database_id;

    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "expense", "지출결의",
            "purchase", "구매요청",
            "overtime", "초과근무",
            "access", "권한신청",
            "etc", "기타결재"
    );

    // 기존 getDatabase() 메서드를 페이지네이션 지원으로 변경
    @Override
    public NotionPageResult getDatabaseWithPaging(String cursor, int pageSize) {
        String url = "https://api.notion.com/v1/databases/" + database_id + "/query";

        try {
        JSONObject body = new JSONObject();
        body.put("page_size", pageSize); // 페이지 크기 설정

        // 커서가 있으면 추가 (다음 페이지)
        if (cursor != null && !cursor.trim().isEmpty()) {
            body.put("start_cursor", cursor);
        }

        // 정렬 추가 (최신순)
        JSONArray sorts = new JSONArray();
        JSONObject sort = new JSONObject();
        sort.put("property", "작성일");
        sort.put("direction", "descending");
        sorts.put(sort);
        body.put("sorts", sorts);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", "Bearer " + token)
                .header("Notion-Version", "2022-06-28")
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();

            if (response.getStatus() != 200) {
                System.err.println("Notion API 에러: " + response.getStatus());
                return new NotionPageResult(); // 빈 결과 반환
            }

        // 응답 파싱
        JsonNode responseBody = response.getBody();
        JSONObject jsonObject = responseBody.getObject();

        NotionPageResult result = new NotionPageResult();
        result.setResults(parseResults(jsonObject.getJSONArray("results")));
        result.setHasMore(jsonObject.getBoolean("has_more"));
        result.setNextCursor(jsonObject.optString("next_cursor", null));

        return result;
        } catch (Exception e) {
            System.err.println("Notion API 호출 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return new NotionPageResult(); // 빈 결과 반환
        }
    }

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

        // 결재 상태 기본값
        properties.put("상태", Map.of("select", Map.of("name", "대기")));

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

    @Override
    public ApprovalDetail getApprovalDetail(String pageId) {
        String url = "https://api.notion.com/v1/pages/" + pageId;

        HttpResponse<JsonNode> response = Unirest.get(url)
                .header("Authorization", "Bearer " + token)
                .header("Notion-Version", "2022-06-28")
                .asJson();

        JSONObject obj = response.getBody().getObject();
        JSONObject props = obj.getJSONObject("properties");

        String title = props.getJSONObject("제목")
                .getJSONArray("title")
                .getJSONObject(0)
                .getString("plain_text");

        String status = props.getJSONObject("상태")
                .getJSONObject("select")
                .getString("name");

        String createdDate = obj.getString("created_time");

        // 내용 가져오기
        String content = "";
        JSONArray contentArray = props.getJSONObject("내용").getJSONArray("rich_text");
        if (contentArray.length() > 0) {
            content = contentArray.getJSONObject(0).getJSONObject("text").getString("content");
        }

        // 구분 가져오기
        String category = "";
        JSONObject categoryObj = props.getJSONObject("구분").optJSONObject("select");
        if (categoryObj != null) {
            category = categoryObj.getString("name");
        }

        // 결재자 가져오기
        String approvers = "";
            try {
                JSONArray approverArray = props.getJSONObject("결재자").getJSONArray("multi_select");
                List<String> approverList = new ArrayList<>();
                for (int i = 0; i < approverArray.length(); i++) {
                    approverList.add(approverArray.getJSONObject(i).getString("name"));
                }
                approvers = String.join(",", approverList);
            } catch (Exception e) {
                approvers = "";
            }
            return new ApprovalDetail(pageId, title, content, category, status, createdDate, approvers);

        }

    @Override
    public List<ApprovalItem> getApprovalList(String filter, String cursor) {
        // 기존 Notion API 호출 로직 (cursor 기반 조회)
        List<ApprovalItem> allItems = this.getDatabaseWithPaging(cursor, 10).getResults();

        if ("all".equals(filter)) {
            return allItems;
        }

        // 상태별 필터링
        return allItems.stream()
                .filter(item ->
                        ("pending".equals(filter) && "대기중".equals(item.getStatus())) ||
                        ("approved".equals(filter) && "승인".equals(item.getStatus())) ||
                        ("rejected".equals(filter) && "반려".equals(item.getStatus()))
                )
                .collect(Collectors.toList());
    }


    // 노션 결과를 파싱하는 메서드
    private List<ApprovalItem> parseResults(JSONArray results) {
        List<ApprovalItem> items = new ArrayList<>();

        try {
            for (int i = 0; i < results.length(); i++) {
                try {
                    JSONObject page = results.getJSONObject(i);
                    JSONObject properties = page.getJSONObject("properties");

                    ApprovalItem item = new ApprovalItem();
                    item.setId(page.getString("id"));

                    // 제목 - 안전하게 파싱
                    try {
                        JSONArray titleArray = properties.getJSONObject("제목").getJSONArray("title");
                        if (titleArray.length() > 0) {
                            item.setTitle(titleArray.getJSONObject(0).getString("plain_text"));
                        } else {
                            item.setTitle("제목 없음");
                        }
                    } catch (Exception e) {
                        System.err.println("제목 파싱 오류: " + e.getMessage());
                        item.setTitle("제목 없음");
                    }

                    // 작성자 - 안전하게 파싱
                    try {
                        JSONArray writerArray = properties.getJSONObject("작성자").getJSONArray("rich_text");
                        if (writerArray.length() > 0) {
                            item.setWriter(writerArray.getJSONObject(0).getString("plain_text"));
                        } else {
                            item.setWriter("작성자 없음");
                        }
                    } catch (Exception e) {
                        System.err.println("작성자 파싱 오류: " + e.getMessage());
                        item.setWriter("작성자 없음");
                    }

                    // 구분 - 안전하게 파싱
                    try {
                        JSONObject categoryObj = properties.getJSONObject("구분").optJSONObject("select");
                        if (categoryObj != null) {
                            item.setCategory(categoryObj.getString("name"));
                        } else {
                            item.setCategory("구분 없음");
                        }
                    } catch (Exception e) {
                        System.err.println("구분 파싱 오류: " + e.getMessage());
                        item.setCategory("구분 없음");
                    }

                    // 작성일 - 안전하게 파싱
                    try {
                        JSONObject dateProperty = properties.optJSONObject("작성일");
                        JSONObject dateObj = (dateProperty != null) ? dateProperty.optJSONObject("date") : null;
                        String dateString = (dateObj != null) ? dateObj.optString("start", null) : null;

                        if (dateString != null && !dateString.isEmpty()) {
                            if (dateString.length() == 10) {
                                // 날짜만 있는 경우 (2025-09-17)
                                LocalDate localDate = LocalDate.parse(dateString);
                                String formatted = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                item.setCreatedDate(formatted);
                            } else {
                                // 전체 날짜시간이 있는 경우 (2025-09-17T08:42:00.000Z)
                                Instant instant = Instant.parse(dateString);
                                ZonedDateTime seoulTime = instant.atZone(ZoneId.of("Asia/Seoul"));
                                String formatted = seoulTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                item.setCreatedDate(formatted);
                            }
                        } else {
                            item.setCreatedDate("날짜 없음");
                        }
                    } catch (Exception e) {
                        System.err.println("작성일 파싱 오류: " + e.getMessage());
                        e.printStackTrace(); // 더 자세한 오류 정보 출력
                        item.setCreatedDate("날짜 없음");
                    }

                    // 결재상태 - 안전하게 파싱
                    try {
                        JSONObject statusObj = properties.getJSONObject("상태").optJSONObject("select");
                        if (statusObj != null) {
                            String status = statusObj.getString("name");
                            // "대기" -> "대기중"으로 변환
                            item.setStatus("대기".equals(status) ? "대기중" : status);
                        } else {
                            item.setStatus("대기중");
                        }
                    } catch (Exception e) {
                        System.err.println("상태 파싱 오류: " + e.getMessage());
                        item.setStatus("대기중");
                    }

                    // 결재자 파싱 - 개별 처리를 위해 배열로 저장
                    try {
                        JSONArray approverArray = properties.getJSONObject("결재자").getJSONArray("multi_select");
                        List<String> approverList = new ArrayList<>();
                        for (int j = 0; j < approverArray.length(); j++) {
                            approverList.add(approverArray.getJSONObject(j).getString("name"));
                        }
                        // 배열을 구분자로 연결해서 저장
                        item.setApprovers(String.join(",", approverList));
                    } catch (Exception e) {
                        System.err.println("결재자 파싱 오류: " + e.getMessage());
                        item.setApprovers("");
                    }

                    items.add(item);

                } catch (Exception e) {
                    System.err.println("페이지 파싱 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("parseResults 전체 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    @Override
    public void updateApprovalStatus(String pageId, String status) {
        String url = "https://api.notion.com/v1/pages/" + pageId;

        JSONObject body = new JSONObject();
        JSONObject properties = new JSONObject();

        // 상태 업데이트
        properties.put("상태", Map.of("select", Map.of("name", status)));
        body.put("properties", properties);

        HttpResponse<JsonNode> response = Unirest.patch(url)
                .header("Authorization", "Bearer " + token)
                .header("Notion-Version", "2022-06-28")
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();
    }


}
