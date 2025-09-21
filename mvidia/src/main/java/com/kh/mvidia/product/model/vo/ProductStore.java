package com.kh.mvidia.product.model.vo;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProductStore { // 창고 테이블

    private String localConde; // 지역 코드
    private String localName;  // 지역명
    private int stock;         // 재고 수량
}
