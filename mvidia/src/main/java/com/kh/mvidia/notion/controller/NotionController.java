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
            System.out.println("🚀 [Controller] exportToNotion 진입 - empNo=" + empNo + ", payDate=" + payDate);

            Map<String, Object> param = new HashMap<>();
            param.put("empNo", empNo);
            param.put("yearMonth", payDate);
            System.out.println("조회 파라미터: " + param);

            List<Salary> salaryList = financeService.getSalary(param);
            if (salaryList == null || salaryList.isEmpty()) {
                System.out.println("⚠️ 급여 데이터 없음 (empNo=" + empNo + ", payDate=" + payDate + ")");
                return ResponseEntity.ok(Map.of(
                        "status", "fail",
                        "message", "해당 지급월의 급여 데이터가 없습니다."
                ));
            }

            Salary salary = salaryList.get(0);
            System.out.println("급여 데이터 확인: empNo=" + salary.getEmpNo() +
                    ", empName=" + salary.getEmpName() +
                    ", payDate=" + salary.getPayDate() +
                    ", netPay=" + salary.getNetPay());

            List<Tax> taxList = financeService.getTaxesByEmpAndMonth(empNo, payDate);
            System.out.println("📌 [Controller] 세금 데이터 건수: " + (taxList != null ? taxList.size() : 0));

            System.out.println("➡️ [Controller] NotionService.insertPayrollToNotion 호출 직전");
            notionService.insertPayrollToNotion(salary, taxList);
            System.out.println("🎉 [Controller] NotionService.insertPayrollToNotion 호출 완료");


            return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                    "message", "급여명세서가 성공적으로 Notion에 업로드되었습니다.",
                    "empNo", empNo,
                    "payDate", payDate
            ));

        } catch (Exception e) {
            System.out.println("❌ [오류 발생] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
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
