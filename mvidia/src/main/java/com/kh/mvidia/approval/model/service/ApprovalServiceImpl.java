package com.kh.mvidia.approval.model.service;

import com.kh.mvidia.approval.model.dto.ApprovalDetail;
import com.kh.mvidia.approval.model.dto.ApprovalItem;
import com.kh.mvidia.notion.dto.NotionPageResult;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.dao.PermissionDao;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PermissionDao pDao;

    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "expense", "지출결의",
            "purchase", "구매요청",
            "overtime", "초과근무",
            "access", "권한신청",
            "etc", "기타결재"
    );

    // 전자결재 등록
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

        JSONObject body = new JSONObject();
        body.put("parent", parent);
        body.put("properties", properties);

        HttpResponse<JsonNode> response = Unirest.post(url)
                .header("Authorization", "Bearer " + token)
                .header("Notion-Version", "2022-06-28")
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();

        return response;
    }

    // 노션 API 호출 - 전체 문서 조회(페이징) - 핵심 메서드
    @Override
    public NotionPageResult getDatabaseWithPaging(String cursor, int pageSize) {
        String url = "https://api.notion.com/v1/databases/" + database_id + "/query";

        try {
            JSONObject body = new JSONObject();
            body.put("page_size", pageSize);

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
                return new NotionPageResult();
            }

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
            return new NotionPageResult();
        }
    }

    // 상세보기
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

        // 작성자 가져오기
        String writer = "";
        JSONArray writerArray = props.getJSONObject("작성자").getJSONArray("rich_text");
        if (writerArray.length() > 0) {
            writer = writerArray.getJSONObject(0).getString("plain_text");
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

        return new ApprovalDetail(pageId, title, content, category, status, createdDate, approvers, writer);
    }

    // 전자결재 상태 업데이트
    @Override
    public void updateApprovalStatus(String pageId, String status) {
        String url = "https://api.notion.com/v1/pages/" + pageId;

        JSONObject body = new JSONObject();
        JSONObject properties = new JSONObject();

        properties.put("상태", Map.of("select", Map.of("name", status)));
        body.put("properties", properties);

        HttpResponse<JsonNode> response = Unirest.patch(url)
                .header("Authorization", "Bearer " + token)
                .header("Notion-Version", "2022-06-28")
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();
    }

    // ===================== 필터링 메서드들 - 통합 =====================

    /**
     * 내가 작성한 문서 조회 (문서함용)
     */
    public List<ApprovalItem> getMyDocuments(Employee loginEmp, String filter) {
        if (loginEmp == null) {
            return Collections.emptyList();
        }

        String currentUser = loginEmp.getEmpLName() + loginEmp.getEmpName();
        NotionPageResult allData = getDatabaseWithPaging("", 1000); // 전체 조회

        List<ApprovalItem> myDocuments = allData.getResults().stream()
                .filter(item -> currentUser.equals(item.getWriter()))
                .collect(Collectors.toList());

        // 필터 적용
        return applyStatusFilter(myDocuments, filter);
    }

    /**
     * 내가 결재해야 할 문서 조회 (승인함용)
     */
    public List<ApprovalItem> getMyApprovalDocuments(Employee loginEmp, String filter) {
        if (loginEmp == null) {
            return Collections.emptyList();
        }

        String currentUser = loginEmp.getEmpLName() + loginEmp.getEmpName();
        NotionPageResult allData = getDatabaseWithPaging("", 1000);

        // 내가 결재해야 할 문서만 필터링
        List<ApprovalItem> myApprovalDocs = allData.getResults().stream()
                .filter(item -> {
                    String approvers = item.getApprovers();
                    return approvers != null && approvers.contains(currentUser);
                })
                .collect(Collectors.toList());

        // 탭별 필터 적용
        if ("pending".equals(filter)) {
            return myApprovalDocs.stream()
                    .filter(item -> "대기".equals(item.getStatus()))
                    .collect(Collectors.toList());
        } else if ("completed".equals(filter)) {
            return myApprovalDocs.stream()
                    .filter(item -> "승인".equals(item.getStatus()))
                    .collect(Collectors.toList());
        }

        return myApprovalDocs;
    }

    /**
     * 결재 대기 문서 수 조회
     */
    public int getPendingApprovalCount(Employee loginEmp) {
        if (loginEmp == null) {
            return 0;
        }

        String currentUser = loginEmp.getEmpLName() + loginEmp.getEmpName();
        NotionPageResult allData = getDatabaseWithPaging("", 1000);

        return (int) allData.getResults().stream()
                .filter(item -> {
                    String approvers = item.getApprovers();
                    boolean isMyApproval = approvers != null && approvers.contains(currentUser);
                    boolean isPending = "대기".equals(item.getStatus());
                    return isMyApproval && isPending;
                })
                .count();
    }

    // ===================== 유틸리티 메서드들 =====================

    /**
     * 상태별 필터링 적용
     */
    private List<ApprovalItem> applyStatusFilter(List<ApprovalItem> items, String filter) {
        if ("all".equals(filter) || filter == null) {
            return items;
        }

        return items.stream()
                .filter(item -> {
                    switch (filter) {
                        case "pending": return "대기".equals(item.getStatus());
                        case "approved": return "승인".equals(item.getStatus());
                        case "rejected": return "반려".equals(item.getStatus());
                        default: return true;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 노션 결과 파싱 - 핵심 파싱 로직
     */
    private List<ApprovalItem> parseResults(JSONArray results) {
        List<ApprovalItem> items = new ArrayList<>();

        try {
            for (int i = 0; i < results.length(); i++) {
                try {
                    JSONObject page = results.getJSONObject(i);
                    JSONObject properties = page.getJSONObject("properties");

                    ApprovalItem item = new ApprovalItem();
                    item.setId(page.getString("id"));

                    // 제목
                    try {
                        JSONArray titleArray = properties.getJSONObject("제목").getJSONArray("title");
                        if (titleArray.length() > 0) {
                            item.setTitle(titleArray.getJSONObject(0).getString("plain_text"));
                        } else {
                            item.setTitle("제목 없음");
                        }
                    } catch (Exception e) {
                        item.setTitle("제목 없음");
                    }

                    // 작성자
                    try {
                        JSONArray writerArray = properties.getJSONObject("작성자").getJSONArray("rich_text");
                        if (writerArray.length() > 0) {
                            item.setWriter(writerArray.getJSONObject(0).getString("plain_text"));
                        } else {
                            item.setWriter("작성자 없음");
                        }
                    } catch (Exception e) {
                        item.setWriter("작성자 없음");
                    }

                    // 구분
                    try {
                        JSONObject categoryObj = properties.getJSONObject("구분").optJSONObject("select");
                        if (categoryObj != null) {
                            item.setCategory(categoryObj.getString("name"));
                        } else {
                            item.setCategory("구분 없음");
                        }
                    } catch (Exception e) {
                        item.setCategory("구분 없음");
                    }

                    // 작성일
                    try {
                        JSONObject dateProperty = properties.optJSONObject("작성일");
                        JSONObject dateObj = (dateProperty != null) ? dateProperty.optJSONObject("date") : null;
                        String dateString = (dateObj != null) ? dateObj.optString("start", null) : null;

                        if (dateString != null && !dateString.isEmpty()) {
                            if (dateString.length() == 10) {
                                LocalDate localDate = LocalDate.parse(dateString);
                                String formatted = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                item.setCreatedDate(formatted);
                            } else {
                                Instant instant = Instant.parse(dateString);
                                ZonedDateTime seoulTime = instant.atZone(ZoneId.of("Asia/Seoul"));
                                String formatted = seoulTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                item.setCreatedDate(formatted);
                            }
                        } else {
                            item.setCreatedDate("날짜 없음");
                        }
                    } catch (Exception e) {
                        item.setCreatedDate("날짜 없음");
                    }

                    // 결재상태
                    try {
                        JSONObject statusObj = properties.getJSONObject("상태").optJSONObject("select");
                        if (statusObj != null) {
                            String status = statusObj.getString("name");
                            item.setStatus(status);
                        } else {
                            item.setStatus("대기");
                        }
                    } catch (Exception e) {
                        item.setStatus("대기");
                    }

                    // 결재자 파싱
                    try {
                        JSONArray approverArray = properties.getJSONObject("결재자").getJSONArray("multi_select");
                        List<String> approverList = new ArrayList<>();
                        for (int j = 0; j < approverArray.length(); j++) {
                            approverList.add(approverArray.getJSONObject(j).getString("name"));
                        }
                        item.setApprovers(String.join(",", approverList));
                    } catch (Exception e) {
                        item.setApprovers("");
                    }

                    items.add(item);

                } catch (Exception e) {
                    System.err.println("페이지 파싱 중 오류 발생: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("parseResults 전체 오류: " + e.getMessage());
        }

        return items;
    }
}