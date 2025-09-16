package com.kh.mvidia.employee.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Employee {

    private String empNo;
    private String empPwd;
    private String email;
    private String empLName;
    private String empName;
    private String empEngLName;
    private String empEngName;
    private String birthday;
    private String deptCode;
    private String deptName;
    private String jobCode;
    private String jobName;
    private String address;
    private String phone;
    private String extNo;
    private String empStatus;
    private String createDate;
    private String modifyDate;
    private String hireDate;
    private String retiredDate;
}