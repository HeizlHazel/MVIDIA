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
            // 데이터 조회 전 파라미터 검증
            System.out.println("🔍 파라미터 확인: empNo=" + empNo + ", payDate=" + payDate);

            Map<String, Object> param = new HashMap<>();
            param.put("empNo", empNo);
            param.put("yearMonth", payDate);
            Salary salary = financeService.getSalary(param).get(0);

            if (salary == null) {
                System.out.println("급여 데이터 없음");

                return ResponseEntity.ok(Map.of(
                        "status", "fail",
                        "message", "급여 데이터가 없습니다"
                ));
            }

            List<Tax> taxList = financeService.getTaxesByEmpAndMonth(empNo, payDate);
            System.out.println("💰 세금 데이터: " + (taxList != null ? taxList.size() : 0));

            notionService.insertPayrollToNotion(salary, taxList);

            return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                    "message", "급여명세서가 성공적으로 Notion에 업로드되었습니다.",
                    "empNo", empNo,
                    "payDate", payDate
            ));

        } catch (Exception e) {
            System.err.println("❌ exportToNotion 오류 발생:");
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
            System.out.println("🔍 데이터 디버깅 시작:");
            System.out.println("   - 요청 사원번호: [" + empNo + "]");
            System.out.println("   - 요청 급여년월: [" + payDate + "]");

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
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    // 전체 급여 데이터 목록 조회 (데이터 확인용)
    @GetMapping("/list-salary-data")
    @ResponseBody
    public ResponseEntity<?> listSalaryData() {
        try {
            // FinanceService에 전체 목록 조회 메소드가 있다면 사용
            // 없다면 임시로 몇 가지 샘플 데이터로 확인

            return ResponseEntity.ok(Map.of(
                    "message", "이 엔드포인트는 데이터베이스의 급여 데이터 목록을 확인하기 위한 것입니다.",
                    "suggestion", "데이터베이스에서 직접 다음 쿼리를 실행해보세요:",
                    "queries", Arrays.asList(
                            "SELECT emp_no, pay_date, emp_name FROM salary WHERE emp_no LIKE '%22010001%'",
                            "SELECT DISTINCT emp_no, pay_date FROM salary ORDER BY pay_date DESC LIMIT 10",
                            "SELECT COUNT(*) FROM salary WHERE emp_no = '22010001'",
                            "SELECT * FROM salary WHERE pay_date LIKE '2025-08%'"
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }


}
