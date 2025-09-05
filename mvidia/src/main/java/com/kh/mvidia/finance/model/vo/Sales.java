package com.kh.mvidia.finance.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Sales {
    private String salesCode;
    private String prodCode;
    private String periodSt;
    private String periodFn;
    private String totalSales;
    private String opProfit;
}
