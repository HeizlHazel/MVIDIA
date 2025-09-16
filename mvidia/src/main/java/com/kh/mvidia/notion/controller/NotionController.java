package com.kh.mvidia.notion.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.sales.model.service.SalesService;
import com.kh.mvidia.sales.model.vo.Sales;
import com.kh.mvidia.finance.model.vo.Tax;
import com.kh.mvidia.notion.service.NotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll")
public class NotionController {

    private final NotionService notionService;
    private final FinanceService financeService;

    @Autowired
    private SalesService salesService;

    @Autowired
    public NotionController(NotionService notionService, FinanceService financeService) {
        this.notionService = notionService;
        this.financeService = financeService;
    }

    @GetMapping("/export-notion")
    @ResponseBody
    public ResponseEntity<?> exportToNotion(@RequestParam String empNo,
                                 @RequestParam String payDate) {
        System.out.println("▶ exportToNotion 호출됨: empNo=" + empNo + ", payDate=" + payDate);

        try{
            Salary salary = financeService.getSalaryByEmpAndMonth(empNo, payDate);
            List<Tax> taxList = financeService.getTaxesByEmpAndMonth(empNo, payDate);

            if (salary == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "fail",
                        "message", "급여 데이터가 없음"
                ));
            }

            notionService.insertPayrollToNotion(salary, taxList);

            return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                    "empNo", empNo,
                    "payDate", payDate
            ));

        } catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("fail: 오류 발생");
        }
    }

    @GetMapping("/revenue-report")
    public String revenueReport(
            @RequestParam(defaultValue = "2025") String year,
            Model model) {

        List<Sales> profitList = salesService.getQuarterlySales(year);

        // 1~4분기 다 채우기 (없는 분기는 매출/이익 0)
        for (int q = 1; q <= 4; q++) {
            int finalQ = q;
            boolean exists = profitList.stream()
                    .anyMatch(s -> s.getQuarter().equals(String.valueOf(finalQ)));

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
