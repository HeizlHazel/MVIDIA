package com.kh.mvidia.product.model.vo;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Schedule { // 품질점검일정 테이블

    private String schNo;   // 품질점검아이디
    private String schName; // 일정명
}
