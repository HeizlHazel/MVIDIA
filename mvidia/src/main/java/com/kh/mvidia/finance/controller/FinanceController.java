package com.kh.mvidia.finance.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Sales;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.OutputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

    @GetMapping("/payroll")
    public String salary(
            @RequestParam(required = false, defaultValue = "202508") String yearMonth,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) String jobCode,
            @RequestParam(required = false) String empName,
            Model model) {

        String viewYearMonth;
        String dbYearMonth;

        if (yearMonth == null || yearMonth.isEmpty()) {
            viewYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        } else if (yearMonth.matches("\\d{6}")) {
            viewYearMonth = yearMonth.substring(0, 4) + "-" + yearMonth.substring(4);

        } else {
            viewYearMonth = yearMonth;
        }

        dbYearMonth = viewYearMonth.replace("-", "");

        if (deptCode == null || deptCode.isEmpty()) {
            deptCode = null;
        }

        if (jobCode == null || jobCode.isEmpty()) {
            jobCode = null;
        }

        if (empName == null || empName.isEmpty()) {
            empName = null;
        }

        Map<String, Object> param = new HashMap<>();
        param.put("yearMonth", dbYearMonth);
        param.put("deptCode", deptCode);
        param.put("jobCode", jobCode);
        param.put("empName", empName);

        List<Salary> salaryList = financeService.getSalaryByCondition(param);

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

    private int calculateOvertimePay(int salary, List<Attendance> records) {
        int baseHourly = salary / 209;
        int ovPrice = 0;

        for (Attendance att : records) {
            LocalDate date = LocalDate.parse(att.getAttDate());
            LocalTime leave = LocalTime.parse(att.getLeavingTime());
            LocalDateTime out = LocalDateTime.of(date, leave);

            DayOfWeek day = date.getDayOfWeek();

            // 18~22시
            LocalDateTime sixPM = date.atTime(18, 0);
            LocalDateTime tenPM = date.atTime(22, 0);

            if (out.isAfter(sixPM)) {
                LocalDateTime end = out.isBefore(tenPM) ? out : tenPM;
                long hours = Duration.between(sixPM, end).toHours();
                ovPrice += baseHourly * hours;
            }

            if (out.isAfter(tenPM)) {
                LocalDateTime sixAM = date.plusDays(1).atTime(6, 0);
                LocalDateTime end = out.isBefore(sixAM) ? out : sixAM;
                long hours = Duration.between(tenPM, end).toHours();
                ovPrice += (int)(baseHourly * hours * 1.5);
            }

            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                long hours = Duration.between(date.atTime(9, 0), out).toHours();
                ovPrice += baseHourly * hours * 2;
            }
        }

        return ovPrice;
    }

    @GetMapping("/revenue-report")
    public String revenueReport(
            @RequestParam(defaultValue = "2025") String year,
            Model model) {

        financeService.mergeQuarterlySales(year);

        List<String> yearList = Arrays.asList("2025", "2024", "2023", "2022");

        List<Sales> salesList = financeService.getQuarterlySales(year);

        Map<String, long[]> productQuarterMap = new HashMap<>();

        Map<String, Object> productSumMap = new HashMap<>();

        long[] totalSales = new long[4];
        long[] totalProfits = new long[4];

        long sumSales = 0;
        long sumProfit = 0;

        for(Sales s : salesList) {
            int qIndex = Integer.parseInt(s.getQuarter()) - 1;

            long sales = Long.valueOf(s.getTotalSales());
            long profit = Long.valueOf(s.getOpProfit());

            productQuarterMap.putIfAbsent(s.getProdName(), new long[4]);
            productQuarterMap.get(s.getProdName())[qIndex] += sales;

            productSumMap.put(s.getProdName(),
                    ((long) productSumMap.getOrDefault(s.getProdName(), 0L)) + sales);

            totalSales[qIndex] += sales;
            totalProfits[qIndex] += profit;

            sumSales += sales;
            sumProfit += profit;
        }

        long ytd = sumSales;

        long prevYearSales = financeService.getYearlySales(String.valueOf(Integer.parseInt(year) - 1));
        double yoy = (prevYearSales > 0)
                ? ((double) (ytd - prevYearSales) / prevYearSales) * 100
                : 0.0;

        double profitRate = (ytd > 0) ? ((double) sumProfit / ytd) * 100 : 0.0;

        int maxSalesQ = 0, minSalesQ = 0;
        long maxSalesVal = totalSales[0], minSalesVal = totalSales[0];

        for (int i = 1; i < 4; i++) {
            if (totalSales[i] > maxSalesVal) {
                maxSalesVal = totalSales[i];
                maxSalesQ = i;
            }
            if (totalSales[i] < minSalesVal) {
                minSalesVal = totalSales[i];
                minSalesQ = i;
            }
        }

        model.addAttribute("maxSales", String.format("%d분기 : %,d억", maxSalesQ + 1, maxSalesVal / 100000000));
        model.addAttribute("minSales", String.format("%d분기 : %,d억", minSalesQ + 1, minSalesVal / 100000000));

        model.addAttribute("ytd", ytd / 100000000); // 억 단위 변환
        model.addAttribute("yoy", String.format("%.1f", yoy));
        model.addAttribute("profitRate", String.format("%.1f", profitRate));

        model.addAttribute("profitList", salesList);
        model.addAttribute("productQuarterMap", productQuarterMap);
        model.addAttribute("productSumMap", productSumMap);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalProfits", totalProfits);
        model.addAttribute("yearList", yearList);
        model.addAttribute("year", year);

        return "finance/revenue-report";
    }
}

