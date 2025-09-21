package com.kh.mvidia.product.model.vo;

import lombok.*;

import java.util.Date;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class QualityCheck {    // 품질기록 테이블

    private String checkId;      // 점검기록ID
    private String schrId;       // 점검일정관리번호
    private String prodCode;     // 제품코드
    private String checkStatus;  // 점검상태 (C완료/P진행중/S지연/W대기)
    private Integer defCount;    // 불량 검사 수
    private Date checkedDate;    // 점검완료날짜
    private String defCode;      // 불량코드
    private Integer statusCount; // 검사 상태별 개수
}
