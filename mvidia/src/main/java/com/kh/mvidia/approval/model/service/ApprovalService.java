package com.kh.mvidia.approval.model.service;

import com.kh.mvidia.approval.model.dto.ApprovalDetail;
import com.kh.mvidia.approval.model.dto.ApprovalItem;
import com.kh.mvidia.approval.model.dto.NotionPageResult;
import com.kh.mvidia.employee.model.vo.Employee;
import kong.unirest.JsonNode;

import kong.unirest.HttpResponse;

import java.util.List;

public interface ApprovalService {

    // 전자결재 신청 - 노션 저장
    public HttpResponse<JsonNode> addPage(String writer, String dept, String date, String title, String approval, String details, String category, String empNo);

    // 노션 API 호출 - 전체 문서 조회(페이징)
    public NotionPageResult getDatabaseWithPaging(String cursor, int pageSize);

    // 상세보기
    public ApprovalDetail getApprovalDetail(String pageId);

    // 전자결재 상태 업데이트
    public void updateApprovalStatus(String pageId, String status);

    // 내 문서 조회
    public List<ApprovalItem> getMyDocuments(Employee loginEmp, String filter);

    // 결재할 문서 조회
    public List<ApprovalItem> getMyApprovalDocuments(Employee loginEmp, String filter);

    // 결재 대기 문서 수 조회
    public int getPendingApprovalCount(Employee loginEmp);

    // 승인/반려 DB에 로그 저장
    public void saveApprovalLog(String pageId, String actorId, String actorName, String action, String reason);
}