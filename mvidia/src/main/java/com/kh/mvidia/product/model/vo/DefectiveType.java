package com.kh.mvidia.product.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class DefectiveType { // 불량 유형 테이블

    private String defCode; // 불량 코드
    private String defType; // 불량 유형
}
