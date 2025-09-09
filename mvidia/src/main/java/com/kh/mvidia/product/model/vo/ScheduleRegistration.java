package com.kh.mvidia.product.model.vo;

import lombok.*;

import java.sql.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ScheduleRegistration { // 품질점검일정등록 테이블

    private String schrId;    // 일정관리번호
    private String schNo;     // 일정명 (일정 테이블 schNo 참조)
    private String catCode;   // 분류 코드 (분류 테이블 catCode 참조)
    private String bpPartner; // 협력사/거래처
    private String empNO;     // 책임자_사번 (사원 테이블 empNo 참조)
    private Date startDate;   // 시작일
    private Date endDate;     // 종료일
    private String colorID;   // 색상 아이디 (테마 테이블 theme_code or colorCode)
    private String details;   // 상세내역
    private int totalCount;   // 전체 검사 수
    private int defCount;     // 불량 검사 수
}
