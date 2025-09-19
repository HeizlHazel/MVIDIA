package com.kh.mvidia.finance.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Comp;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.sales.model.service.SalesService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.OutputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private SalesService salesService;

    @GetMapping("/main")
    public String mainPage(Model model) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

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

        Map<String, Object> revenueSummary =
                salesService.getQuarterlySummary(year, quarter);
        model.addAttribute("revenueSummary", revenueSummary);
        model.addAttribute("year", year);
        model.addAttribute("quarter", quarter);

        // 부품 현황
        List<Comp> compList = financeService.getAllComponents();
        applyMinQtyAndStatus(compList, model);
        List<Comp> lowList = compList.stream()
                .filter(c -> Integer.parseInt(c.getQty()) < getMinQty(c.getCpCode()))
                .toList();
        model.addAttribute("lowList", lowList);

        // 현재 분기 상세 수익 (제품별)
        Map<String, Long> productRevenueMap =
                salesService.getQuarterlyProductRevenue(year, quarter);
        model.addAttribute("productRevenueMap", productRevenueMap);

        return "finance/main";
    }

    @GetMapping("/payroll")
    public String salary(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) String jobCode,
            @RequestParam(required = false) String empName,
            Model model) {

        String nowYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        String dbYearMonth;

        if (yearMonth == null || yearMonth.isEmpty()) {
            dbYearMonth = nowYearMonth;
        } else {
            dbYearMonth = yearMonth;
        }

        String viewYearMonth = dbYearMonth;

        Map<String, Object> param = new HashMap<>();
        param.put("yearMonth", dbYearMonth);
        param.put("deptCode", deptCode);
        param.put("jobCode", jobCode);
        param.put("empName", empName);

        List<Salary> salaryList = financeService.getFilteredSalary(dbYearMonth, deptCode, jobCode, empName);

        model.addAttribute("salaryList", salaryList);
        model.addAttribute("yearMonth", viewYearMonth);
        model.addAttribute("deptCode", deptCode);
        model.addAttribute("jobCode", jobCode);
        model.addAttribute("empName", empName);

        return "finance/payroll";
    }

    @GetMapping("/salary-pdf")
    public void exportSalaryPdf(
            @RequestParam String empNo,
            @RequestParam String payDate,
            HttpServletResponse response) {
        try {
            Salary salary = financeService.getSalaryByEmpAndMonth(empNo, payDate);

            Context context = new Context();
            context.setVariable("salary", salary);

            String html = templateEngine.process("salary-pdf", context);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=salary_" + empNo + "_" + payDate + ".pdf");

            try (OutputStream os = response.getOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, new ClassPathResource("/templates/finance/").getURL().toString());
                builder.toStream(os);
                builder.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/searchSalary")
    @ResponseBody
    public List<Salary> searchSalary(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) String jobCode,
            @RequestParam(required = false) String keyword) {

        return financeService.getFilteredSalary(yearMonth, deptCode, jobCode, keyword);
        }

    private static final Map<String, Integer> MIN_QTY_MAP = new HashMap<>();
    static {
        MIN_QTY_MAP.put("CP0001", 400);
        MIN_QTY_MAP.put("CP0002", 100);
        MIN_QTY_MAP.put("CP0003", 60);
        MIN_QTY_MAP.put("CP0004", 80);
        MIN_QTY_MAP.put("CP0005", 200);
    }

    public int getMinQty(String cpCode) {
        return MIN_QTY_MAP.getOrDefault(cpCode, 0);
    }

    private void applyMinQtyAndStatus(List<Comp> compList, Model model) {
        int normalCnt = 0;
        int lowCnt = 0;

        Map<String, String> statusMap = new HashMap<>();

        for(Comp c : compList) {
            int minQty = getMinQty((c.getCpCode()));
            int qty = Integer.parseInt(c.getQty());

            if (qty >= minQty) {
                statusMap.put(c.getCpCode(), "정상");
                normalCnt++;
            } else  {
                statusMap.put(c.getCpCode(), "부족");
                lowCnt++;
            }
        }

        model.addAttribute("normalCnt", normalCnt);
        model.addAttribute("lowCnt", lowCnt);
        model.addAttribute("statusMap", statusMap);
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        List<Comp> compList = financeService.getAllComponents();
        applyMinQtyAndStatus(compList, model);
        model.addAttribute("compList", compList);
        return "finance/inventory";
    }

    @GetMapping("/inventory/search")
    public String searchInventory(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                                  @RequestParam(value = "status", defaultValue = "") String status,
                                  @RequestParam(value = "localCode", required = false) String localCode,
                                  Model model) {

        List<Comp> compList =  financeService.searchComponents(keyword, localCode, status);

        // 상태별 필터링
        applyMinQtyAndStatus(compList, model);

        model.addAttribute("compList", compList);
        return "finance/inventory :: componentTableFrag";
    }

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



}

