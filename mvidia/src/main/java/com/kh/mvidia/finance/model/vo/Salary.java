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
    private String birthDay;
    private String accountNo;
    private String bankName;
    private String accountFormat;

    public String getFormattedAccountNo() {



        if (accountNo == null) return "";
        if (accountFormat == null || accountFormat.isEmpty()) {
            return accountNo;
        }

        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (char c : accountFormat.toCharArray()) {
            if (c == '#') {
                if (idx < accountNo.length()) {
                    sb.append(accountNo.charAt(idx++));
                }
            } else  {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void setFormat(String accountFormat) {
        this.accountFormat = accountFormat;
    }

    private String extendOv;  // 추가수당 항목
    private String nightOv;
    private String weekendOv;
    private String tripOv;

    private String nationalPension; // 세금 항목
    private String healthInsurance;
    private String employmentInsurance;
    private String incomeTax;
    private String localTax;
    private String deductAmt;

    private String totalPay; // 총지급액
    private String netPay;   // 실지급액
}
