package com.kh.mvidia.product.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProductQuality { // 생산제품정보 테이블


    private String prodCode;   // 제품 코드
    private String prodName;   // 제품명
    private int price;         // 단가
    private String localCode;  // 지역 코드 -> 재고 수량(stock) 참조
    private String prodStatus; // 상태 -> 상태 테이블 참조
    private int stock; // 재고 수량 (재고 테이블의 local_code join으로 가져오기)
    private String statusName; // 처리상태명 (상태 테이블의 status join으로 가져오기)
}
