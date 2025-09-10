package com.kh.mvidia.product.model.vo;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class DefectiveProduction {  // 제품불량등록 테이블

    private String defNo;      // 불량 아이디
    private String prodCode;   // 제품 코드 (생산제품정보 테이블의 prodCode 참조)
    private Integer defQty;        // 불량 수량
    private String defCode;    // 불량 코드 (불량유형 테이블의 defCode 참조)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date occurDate;    // 발생일
    private String defStatus;  // 상태 (상태 테이블의 status)
    private String remarks;    // 비고
    private String prodName;   // 제품명 (제품 테이블의 prodName join으로 가져오기)
    private String defType;    // 불량유형명 (뷸량유형 테이블의 defType join으로 가져오기)
    private String statusName; // 처리상태명 (상태 테이블의 status join으로 가져오기)
    
}
