package com.kh.mvidia.notion.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Sales;
import com.kh.mvidia.finance.model.vo.Tax;
import com.kh.mvidia.notion.service.NotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
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

    @GetMapping("/revenue-report")
    public String revenueReport(
            @RequestParam(defaultValue = "2025") String year,
            Model model) {

        List<Sales> profitList = financeService.getQuarterlySales(year);

        // 1~4분기 다 채우기 (없는 분기는 매출/이익 0)
        for (int q = 1; q <= 4; q++) {
            boolean exists = profitList.stream()
                    .anyMatch(s -> s.getQuarter().equals(String.valueOf(q)));

            if (!exists) {
                Sales zeroData = new Sales(
                        null,           // salesCode
                        null,           // prodCode
                        null,           // periodSt
                        null,           // periodFn
                        "0",            // totalSales
                        "0",            // opProfit
                        "전체",          // prodName (또는 "합계")
                        year,
                        String.valueOf(q)
                );
                profitList.add(zeroData);
            }
        }

        // 분기순으로 정렬
        profitList.sort(Comparator.comparing(Sales::getQuarter));

        model.addAttribute("profitList", profitList);
        model.addAttribute("year", year);
        return "finance/revenue-report";
    }
}
