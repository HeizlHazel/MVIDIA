package com.kh.mvidia.product.model.vo;

import lombok.*;

import java.sql.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProgressChart { // 작업진행현황 테이블

    private String taskCode; // 작업 코드
    private String taskId;   // 작업명
    private String itemCode; // 품목 코드
    private Date startDate; // 작업_시작예정일
    private Date endDate;   // 작업_종료예정일
    private Double progRate;  // 작업_진행량
    private String schName;

}
