package com.kh.mvidia.finance.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.sales.model.vo.Sales;
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

        if (yearMonth.matches("\\d{6}")) {

            viewYearMonth = yearMonth.substring(0, 4) + "-" + yearMonth.substring(4);
        } else {
            viewYearMonth = yearMonth;
        }

        String dbYearMonth = viewYearMonth;

        Map<String, Object> param = new HashMap<>();
        param.put("yearMonth", dbYearMonth);
        param.put("deptCode", deptCode);
        param.put("jobCode", jobCode);
        param.put("empName", empName);

        List<Salary> salaryList = financeService.getSalary(param);

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
}

