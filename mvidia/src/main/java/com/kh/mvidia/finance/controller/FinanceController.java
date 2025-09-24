package com.kh.mvidia.finance.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Comp;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.sales.model.service.SalesService;
import com.kh.mvidia.sales.model.vo.Sales;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SalesService salesService;

    // 메인 대시보드
    @GetMapping("/main")
    public String mainPage(Model model) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 급여 관련 데이터
        List<Salary> salaryList = financeService.getFilteredSalary(yearMonth, null, null, null);
        int empCount = salaryList.size();
        int totalNetPay = salaryList.stream()
                .mapToInt(s -> Integer.parseInt(s.getNetPay() != null ? s.getNetPay() : "0"))
                .sum();
        int avgNetPay = empCount > 0 ? totalNetPay / empCount : 0;

        model.addAttribute("empCount", empCount);
        model.addAttribute("totalNetPay", totalNetPay);
        model.addAttribute("avgNetPay", avgNetPay);
        model.addAttribute("thisMonth", yearMonth);

        // 현재 분기 계산
        int month = now.getMonthValue();
        int quarter = (month - 1) / 3 + 1;

        Map<String, Object> revenueSummary = salesService.getQuarterlySummary(year, quarter);
        model.addAttribute("revenueSummary", revenueSummary);
        model.addAttribute("year", year);
        model.addAttribute("quarter", quarter);

        // 부품 현황
        List<Comp> compList = financeService.getAllComponents();
        applyMinQtyAndStatus(compList, model);

        List<Comp> lowList = compList.stream()
                .filter(c -> Integer.parseInt(c.getQty()) < Integer.parseInt(c.getMinQty()))
                .toList();
        model.addAttribute("lowList", lowList);

        // 제품별 수익 순위 Top3
        Map<String, Long> productRevenueMap = salesService.getQuarterlyProductRevenue(year, quarter);
        List<Map.Entry<String, Long>> productRevenueTop3 = productRevenueMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .collect(Collectors.toList());
        model.addAttribute("productRevenueTop3", productRevenueTop3);

        // 연간 매출/영업이익 (억 단위)
        List<Sales> yearlySalesList = salesService.getQuarterlySales(year);

        double[] totalSalesArr = new double[4];
        double[] totalProfitArr = new double[4];
        long ytd = 0;

        for (Sales s : yearlySalesList) {
            int qIndex = Integer.parseInt(s.getQuarter()) - 1;
            long sales = Long.parseLong(s.getTotalSales());
            long profit = Long.parseLong(s.getOpProfit());

            totalSalesArr[qIndex] += sales / 100000000.0;
            totalProfitArr[qIndex] += profit / 100000000.0;

            ytd += sales;
        }

        model.addAttribute("totalSales", totalSalesArr);
        model.addAttribute("totalProfits", totalProfitArr);
        model.addAttribute("ytd", ytd / 100000000);

        return "finance/main";
    }

    /** 급여 현황 페이지 */
    @GetMapping("/payroll")
    public String salary(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) String jobCode,
            @RequestParam(required = false) String empName,
            Model model) {

        String nowYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String dbYearMonth = (yearMonth == null || yearMonth.isEmpty()) ? nowYearMonth : yearMonth;

        List<Salary> salaryList = financeService.getFilteredSalary(dbYearMonth, deptCode, jobCode, empName);

        model.addAttribute("salaryList", salaryList);
        model.addAttribute("yearMonth", dbYearMonth);
        model.addAttribute("deptCode", deptCode);
        model.addAttribute("jobCode", jobCode);
        model.addAttribute("empName", empName);

        return "finance/payroll";
    }

    /** 급여명세서 PDF Notion 업로드 */
    @GetMapping("/salary-pdf-notion")
    public void exportSalaryPdfAndUploadNotion(
            @RequestParam String empNo,
            @RequestParam String yearMonth,
            HttpServletResponse response) throws IOException {
        Salary salary = financeService.getSalaryByEmpAndMonth(empNo, yearMonth);

        if (salary == null) {
            response.setContentType("text/html; charset=UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write("<script>alert('선택한 월의 급여 데이터가 없습니다.'); location.href='/finance/payroll';</script>");
                writer.flush();
            }
            return;
        }

        // ✅ Notion 업로드 처리
        // notionService.uploadSalaryPdf(salary);

        response.setContentType("text/html; charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("<script>alert('노션 업로드가 완료되었습니다.'); location.href='/finance/payroll';</script>");
            writer.flush();
        }
    }



    /** 재고 현황 적용 */
    private void applyMinQtyAndStatus(List<Comp> compList, Model model) {
        int normalCnt = 0;
        int lowCnt = 0;
        Map<String, String> statusMap = new HashMap<>();

        for (Comp c : compList) {
            int qty = Integer.parseInt(c.getQty());
            int minQty = Integer.parseInt(c.getMinQty());

            if (qty >= minQty) {
                statusMap.put(c.getCpCode(), "정상");
                normalCnt++;
            } else {
                statusMap.put(c.getCpCode(), "부족");
                lowCnt++;
            }
        }

        model.addAttribute("normalCnt", normalCnt);
        model.addAttribute("lowCnt", lowCnt);
        model.addAttribute("statusMap", statusMap);
    }

    // 창고 코드 -> 이름 매칭 메소드
    private Map<String, String> getLocalCodeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("100", "서울 창고");
        map.put("110", "부산 창고");
        map.put("300", "대만 창고");
        map.put("400", "캘리포니아 창고");
        map.put("410", "텍사스 창고");
        return map;
    }

    /** 재고 현황 페이지 */
    @GetMapping("/inventory")
    public String inventory(Model model) {
        List<Comp> compList = financeService.getAllComponents();
        applyMinQtyAndStatus(compList, model);
        model.addAttribute("compList", compList);

        // 창고 코드 → 이름 매핑
        model.addAttribute("localCodeMap", getLocalCodeMap());

        return "finance/inventory";
    }

    @GetMapping("/inventory/search")
    public String searchInventory(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "status", defaultValue = "") String status,
            @RequestParam(value = "localCode", required = false) String localCode,
            Model model) {

        List<Comp> compList = financeService.searchComponents(keyword, localCode, status);
        applyMinQtyAndStatus(compList, model);
        model.addAttribute("compList", compList);

        model.addAttribute("localCodeMap", getLocalCodeMap());

        return "finance/inventory :: componentTableFrag";
    }


    /** 급여 Ajax 검색 */
    @GetMapping("/payroll/search")
    public String searchPayrollFragment(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) String jobCode,
            @RequestParam(required = false) String empName,
            Model model) {
        List<Salary> salaryList = financeService.getFilteredSalary(yearMonth, deptCode, jobCode, empName);
        model.addAttribute("salaryList", salaryList);

        return "finance/payroll :: salaryTableFrag";
    }

    /** 전체 부품 조회 (Ajax) */
    @GetMapping("/getComponents")
    @ResponseBody
    public List<Comp> getComponents() {
        return financeService.getAllComponents();
    }

    /** 재고 수정 (입출고) */
    @PostMapping("/updateStock")
    @ResponseBody
    public String updateStock(@RequestParam String cpCode,
                              @RequestParam int qty,
                              @RequestParam String type) {
        int result = financeService.updateStock(cpCode, qty, type);

        if (result == -1) {
            return "not_enough"; // 출고 불가
        }
        return result > 0 ? "success" : "fail";
    }
}
