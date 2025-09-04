package com.kh.mvidia.employee.controller;

import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService empService;

    @PostMapping("login.emp")
    public String loginEmp(Employee emp){
        int result =  empService.loginEmp(emp);
        return "/common/mainPage";
    }
}
