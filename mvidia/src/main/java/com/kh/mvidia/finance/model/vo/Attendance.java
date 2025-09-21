package com.kh.mvidia.finance.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Attendance {
    private String attNo;
    private String empNo;
    private String hrmNo;
    private String attDate;
    private String arrivingTime;
    private String leavingTime;
    private String attStatus;
    private String minQty;


}
