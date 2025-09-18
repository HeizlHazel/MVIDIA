package com.kh.mvidia.finance.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Comp;
import com.kh.mvidia.finance.model.vo.Salary;
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
                                  Model model) {

        List<Comp> compList = (keyword != null && !keyword.trim().isEmpty())
                ? financeService.searchComponents(keyword)
                : financeService.getAllComponents();

        // 상태별 필터링
        applyMinQtyAndStatus(compList, model);

        if ("normal".equals(status)) {
            compList.removeIf(c -> Integer.parseInt(c.getQty()) < getMinQty(c.getCpCode()));
        } else if ("low".equals(status)) {
            compList.removeIf(c -> Integer.parseInt(c.getQty()) >= getMinQty(c.getCpCode()));
        }

        model.addAttribute("compList", compList);
        return "finance/inventory :: componentTableFrag";
    }

}

