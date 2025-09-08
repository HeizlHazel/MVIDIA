package com.kh.mvidia.finance.controller;

import com.kh.mvidia.finance.model.service.FinanceService;
import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            // 값이 없으면 현재 달로
            viewYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } else if (yearMonth.matches("\\d{6}")) {
            // "202508" → "2025-08"
            viewYearMonth = yearMonth.substring(0, 4) + "-" + yearMonth.substring(4);
        } else {
            // 이미 yyyy-MM 형태로 들어온 경우
            viewYearMonth = yearMonth;
        }

        // DB 검색용은 항상 yyyyMM
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

        // 조건들을 Map에 담아서 전달
        Map<String, Object> param = new HashMap<>();
        param.put("yearMonth", dbYearMonth);
        param.put("deptCode", deptCode);
        param.put("jobCode", jobCode);
        param.put("empName", empName);

        // Service 호출
        List<Salary> salaryList = financeService.getSalaryByCondition(param);

        // 화면에 전달
        model.addAttribute("salaryList", salaryList);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("deptCode", deptCode);
        model.addAttribute("jobCode", jobCode);
        model.addAttribute("empName", empName);

        return "finance/payroll";
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

}

