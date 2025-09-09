package com.kh.mvidia.product.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Category { // 분류 테이블

    private  String catCode; // 분류 코드
    private  String catName; // 분류명
}
