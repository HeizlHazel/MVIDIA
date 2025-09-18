package com.kh.mvidia.approval.model.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ApprovalDetail {

    private String id;        // 노션 페이지 ID
    private String title;     // 제목
    private String content;   // 본문(내용)
    private String category;  // 구분
    private String status;    // 결재상태
    private String createdDate; // 작성일
    private String approvers;
    private String writer;

}
