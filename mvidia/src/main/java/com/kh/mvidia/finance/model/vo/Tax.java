package com.kh.mvidia.finance.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Tax {
    private String taxCode;
    private String empNo;
    private String payDate;
    private String amount;
}
