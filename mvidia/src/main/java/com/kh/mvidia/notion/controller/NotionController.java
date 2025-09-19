package com.kh.mvidia.notion.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.sales.model.service.SalesService;
import com.kh.mvidia.finance.model.vo.Tax;
import com.kh.mvidia.notion.service.NotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    // 기존 export 메소드에 데이터 조회 디버깅 추가
    @GetMapping("/export-notion")
    @ResponseBody
    public ResponseEntity<?> exportToNotion(@RequestParam String empNo,
                                            @RequestParam String payDate) {

        try {

            Map<String, Object> param = new HashMap<>();
            param.put("empNo", empNo);
            param.put("yearMonth", payDate);
            Salary salary = financeService.getSalary(param).get(0);

            if (salary == null) {

                return ResponseEntity.ok(Map.of(
                        "status", "fail",
                        "message", "급여 데이터가 없습니다"
                ));
            }

            List<Tax> taxList = financeService.getTaxesByEmpAndMonth(empNo, payDate);
            notionService.insertPayrollToNotion(salary, taxList);

            return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                    "message", "급여명세서가 성공적으로 Notion에 업로드되었습니다.",
                    "empNo", empNo,
                    "payDate", payDate
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "노션 업로드 중 오류가 발생했습니다: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

    // 데이터 존재 여부 확인용 테스트 엔드포인트
    @GetMapping("/debug-data")
    @ResponseBody
    public ResponseEntity<?> debugData(@RequestParam String empNo,
                                       @RequestParam String payDate) {
        try {
            // 급여 데이터 조회
            Salary salary = financeService.getSalaryByEmpAndMonth(empNo, payDate);
            // 세금 데이터 조회
            List<Tax> taxList = financeService.getTaxesByEmpAndMonth(empNo, payDate);

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("requestEmpNo", empNo);
            debugInfo.put("requestPayDate", payDate);
            debugInfo.put("salaryFound", salary != null);
            debugInfo.put("taxCount", taxList != null ? taxList.size() : 0);

            if (salary != null) {
                debugInfo.put("salaryDetails", Map.of(
                        "empNo", salary.getEmpNo(),
                        "empName", salary.getEmpName(),
                        "payDate", salary.getPayDate(),
                        "netPay", salary.getNetPay()
                ));
            }

            if (taxList != null && !taxList.isEmpty()) {
                debugInfo.put("taxDetails", taxList.stream()
                        .map(tax -> Map.of(
                                "taxCode", tax.getTaxCode(),
                                "amount", tax.getAmount()
                        ))
                        .collect(Collectors.toList()));
            }

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }
}
