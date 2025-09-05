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
import java.util.List;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @GetMapping("/payroll")
    public String salary(@RequestParam(defaultValue = "202508") String yearMonth, Model model) {
        List<Salary> salaryList = financeService.getSalaryByMonth(yearMonth);
        model.addAttribute("salaryList", salaryList);
        model.addAttribute("yearMonth", yearMonth);
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

