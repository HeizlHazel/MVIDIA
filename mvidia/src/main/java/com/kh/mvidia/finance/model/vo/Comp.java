package com.kh.mvidia.finance.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Comp {
    private String cpCode;
    private String cpName;
    private String price;
    private String qty;
    private String localCode;
    private String minQty;
}
