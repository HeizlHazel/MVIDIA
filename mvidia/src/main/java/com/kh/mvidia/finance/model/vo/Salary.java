package com.kh.mvidia.finance.model.vo;

import com.kh.mvidia.employee.model.vo.Employee;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Salary {
    private String payDate;
    private String empNo;
    private String salary;
    private String ovPrice;
    private String bonus;
    private String bonusAmt;
    private String deductAmt;
    private String netPay;
    private String empName;
    private String deptCode;
    private String deptName;
    private String jobCode;
    private String jobName;
}
