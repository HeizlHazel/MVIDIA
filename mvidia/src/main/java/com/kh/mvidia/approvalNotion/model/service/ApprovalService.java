package com.kh.mvidia.approvalNotion.model.service;

import kong.unirest.JsonNode;

import kong.unirest.HttpResponse;

public interface ApprovalService {

    // 전자결재 게시판 조회
    public String getDatabase();

    // 전자결재 등록(노션 연동)
    HttpResponse<JsonNode> addPage(String applyWriter, String applyDept, String applyDate, String applyTitle, String approval, String details);
}
