package com.kh.mvidia.approvalNotion.model.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ApprovalItem {

    private String id;
    private String title;
    private String writer;
    private String category;
    private String createdDate;
    private String status;
    private String approvers;

}
