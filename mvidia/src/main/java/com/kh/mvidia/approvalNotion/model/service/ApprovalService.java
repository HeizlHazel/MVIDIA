package com.kh.mvidia.approvalNotion.model.service;

import com.kh.mvidia.approvalNotion.model.dto.ApprovalDetail;
import com.kh.mvidia.approvalNotion.model.dto.ApprovalItem;
import com.kh.mvidia.approvalNotion.model.dto.NotionPageResult;
import kong.unirest.JsonNode;

import kong.unirest.HttpResponse;
import kong.unirest.json.JSONArray;

import java.util.List;

public interface ApprovalService {

    // 전자결재 등록(노션 연동)
    HttpResponse<JsonNode> addPage(String applyWriter, String applyDept, String applyDate, String applyTitle, String approval, String details, String category);

    public NotionPageResult getDatabaseWithPaging(String cursor, int pageSize);

    ApprovalDetail getApprovalDetail(String pageId);

    void updateApprovalStatus(String pageId, String status);

    public List<ApprovalItem> getApprovalList(String filter, String cursor);

}
