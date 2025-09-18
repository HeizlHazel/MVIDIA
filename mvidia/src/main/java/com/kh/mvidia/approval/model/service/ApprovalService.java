package com.kh.mvidia.approval.model.service;

import com.kh.mvidia.approval.model.dto.ApprovalDetail;
import com.kh.mvidia.approval.model.dto.ApprovalItem;
import com.kh.mvidia.notion.dto.NotionPageResult;
import com.kh.mvidia.employee.model.vo.Employee;
import kong.unirest.JsonNode;

import kong.unirest.HttpResponse;

import java.util.List;

public interface ApprovalService {


    public HttpResponse<JsonNode> addPage(String writer, String dept, String date, String title, String approval, String details, String category);

    public NotionPageResult getDatabaseWithPaging(String cursor, int pageSize);

    public ApprovalDetail getApprovalDetail(String pageId);

    public void updateApprovalStatus(String pageId, String status);

    public List<ApprovalItem> getMyDocuments(Employee loginEmp, String filter);

    public List<ApprovalItem> getMyApprovalDocuments(Employee loginEmp, String filter);

    public int getPendingApprovalCount(Employee loginEmp);

}