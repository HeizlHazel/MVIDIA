package com.kh.mvidia.notion.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import com.kh.mvidia.notion.service.NotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll")
public class NotionController {

    private final NotionService notionService;
    private final FinanceService financeService;

    @Autowired
    public NotionController(NotionService notionService, FinanceService financeService) {
        this.notionService = notionService;
        this.financeService = financeService;
    }

    @GetMapping("/export-notion")
    @ResponseBody
    public String exportToNotion(@RequestParam String empNo,
                                 @RequestParam String payDate) {
        System.out.println("▶ exportToNotion 호출됨: empNo=" + empNo + ", payDate=" + payDate);

        Salary salary = financeService.getSalaryByEmpAndMonth(empNo, payDate);
        List<Tax> taxList = financeService.getTaxesByEmpAndMonth(empNo, payDate);

        System.out.println("▶ DB 조회 결과: " + salary);
        System.out.println("▶ 세금 항목 조회 결과: " + taxList);

        if (salary == null) {
            return "fail";
        }

        notionService.insertPayrollToNotion(salary, taxList);

        return "success";
    }
}
