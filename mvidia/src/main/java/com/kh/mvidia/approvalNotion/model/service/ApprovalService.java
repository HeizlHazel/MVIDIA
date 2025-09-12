package com.kh.mvidia.approvalNotion.model.service;

import kong.unirest.JsonNode;

import kong.unirest.HttpResponse;

public interface ApprovalService {

    // 전자결재 게시판 조회
    public String getDatabase();

    // 전자결재 신청(게시판 등록)
    HttpResponse<JsonNode> addApproval(String title, String category, String writer);

}
