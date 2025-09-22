package com.kh.mvidia.sales.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class SalesHistory {
    private String saleId;
    private String prodCode;
    private String saleDate;
    private String qty;
    private String profitRate;
}
