package com.kh.mvidia.finance.model.vo;

import com.kh.mvidia.employee.model.vo.Employee;
import lombok.*;

import java.util.List;

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
    private String empName;
    private String deptCode;
    private String deptName;
    private String jobCode;
    private String jobName;

    private String extendOv;  // 추가수당 항목
    private String nightOv;
    private String weekendOv;
    private String tripOv;

    private String incomeTax; // 세금 항목
    private String nationalPension;
    private String healthInsurance;
    private String employmentInsurance;
    private String localTax;
    private String deductAmt;

    private String totalPay; // 총지급액
    private String netPay;   // 실지급액
}
