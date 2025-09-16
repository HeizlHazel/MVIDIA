package com.kh.mvidia.finance.model.service;
import com.kh.mvidia.finance.model.dao.FinanceDao;
import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private FinanceDao financeDao;

    @Override
    public Salary getSalaryByEmpAndMonth(String empNo, String payDate) {
        String yearMonth = payDate.replace("-", "");

        List<Salary> list = financeDao.selectSalary(sqlSession,
                Map.of("empNo", empNo, "yearMonth", yearMonth));

        return list.isEmpty() ? null : list.get(0);
    }


    @Override
    public List<Salary> getSalary(Map<String, Object> param) {
        List<Salary> salaryList = financeDao.selectSalary(sqlSession, param);

        String yearMonth = (String) param.get("yearMonth");

        for (Salary s : salaryList) {
            int baseSalary = Integer.parseInt(s.getSalary());
            int bonusPercent = Integer.parseInt(s.getBonus());
            int baseHourly = baseSalary / 209;

            List<Attendance> records =
                    financeDao.selectAttendanceByEmpMonth(sqlSession, s.getEmpNo(), yearMonth);

            int extendedAmt = calculateExtendedPay(baseHourly, records);
            int nightAmt    = calculateNightPay(baseHourly, records);
            int weekendAmt  = calculateWeekendPay(baseHourly, records);
            int tripAmt     = calculateTripPay(records);

            upsertOvertimes(s, yearMonth.replace("-", ""), extendedAmt, nightAmt, weekendAmt, tripAmt);

            int totalOv = extendedAmt + nightAmt + weekendAmt + tripAmt;
            financeDao.updateOvPrice(sqlSession, s.getEmpNo(), yearMonth.replace("-", ""), totalOv);
            s.setOvPrice(String.valueOf(totalOv));

            int bonusAmt = baseSalary * bonusPercent / 100;
            s.setBonusAmt(String.valueOf(bonusAmt));

            int totalPay = baseSalary + totalOv + bonusAmt;

            // 세금 계산
            int taxNP = (int)(totalPay * 0.045);
            int taxHI = (int)(totalPay * 0.035);
            int taxUE = (int)(totalPay * 0.009);
            int taxIC = (int)(totalPay * 0.05);
            int taxLS = (int)(totalPay * 0.005);

            int deductAmt = taxNP + taxHI + taxUE + taxIC + taxLS;
            int netPay    = totalPay - deductAmt;

            // 값 세팅
            s.setExtendOv(String.valueOf(extendedAmt));
            s.setNightOv(String.valueOf(nightAmt));
            s.setWeekendOv(String.valueOf(weekendAmt));
            s.setTripOv(String.valueOf(tripAmt));
            s.setTotalPay(String.valueOf(totalPay));

            s.setIncomeTax(String.valueOf(taxIC));
            s.setNationalPension(String.valueOf(taxNP));
            s.setHealthInsurance(String.valueOf(taxHI));
            s.setEmploymentInsurance(String.valueOf(taxUE));
            s.setLocalTax(String.valueOf(taxLS));

            s.setDeductAmt(String.valueOf(deductAmt));
            s.setNetPay(String.valueOf(netPay));

            // DB 반영
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0001", taxNP);
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0002", taxHI);
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0003", taxUE);
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0004", taxIC);
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0005", taxLS);
        }

        return salaryList;
    }


    @Override
    public List<Tax> getTaxesByEmpAndMonth(String empNo, String payDate) {
        return financeDao.selectTaxesByEmpAndMonth(sqlSession, empNo, payDate);
    }

    private void upsertOvertimes(Salary s, String yearMonth,
                                 int extendedAmt, int nightAmt, int weekendAmt, int tripAmt) {
        financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0001", extendedAmt);
        financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0002", nightAmt);
        financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0003", weekendAmt);
        financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0004", tripAmt);
    }

    private int calculateExtendedPay(int baseHourly, List<Attendance> records) {
        int extendedAmt = 0;
        for (Attendance att : records) {
            LocalDate date = LocalDate.parse(att.getAttDate());
            LocalTime leave = LocalTime.parse(att.getLeavingTime());
            LocalDateTime out = LocalDateTime.of(date, leave);

            LocalDateTime sixPM = date.atTime(18, 0);
            LocalDateTime tenPM = date.atTime(22, 0);

            if (out.isAfter(sixPM)) {
                LocalDateTime end = out.isBefore(tenPM) ? out : tenPM;
                long hours = Duration.between(sixPM, end).toHours();
                extendedAmt += baseHourly * hours;
            }
        }
        return extendedAmt;
    }

    private int calculateNightPay(int baseHourly, List<Attendance> records) {
        int nightAmt = 0;
        for (Attendance att : records) {
            LocalDate date = LocalDate.parse(att.getAttDate());
            LocalTime leave = LocalTime.parse(att.getLeavingTime());
            LocalDateTime out = LocalDateTime.of(date, leave);

            LocalDateTime tenPM = date.atTime(22, 0);
            LocalDateTime sixAM = date.plusDays(1).atTime(6, 0);

            if (out.isAfter(tenPM)) {
                LocalDateTime end = out.isBefore(sixAM) ? out : sixAM;
                long hours = Duration.between(tenPM, end).toHours();
                nightAmt += (int)(baseHourly * hours * 1.5);
            }
        }
        return nightAmt;
    }

    private int calculateWeekendPay(int baseHourly, List<Attendance> records) {
        int weekendAmt = 0;
        for (Attendance att : records) {
            LocalDate date = LocalDate.parse(att.getAttDate());
            LocalTime leave = LocalTime.parse(att.getLeavingTime());
            LocalDateTime out = LocalDateTime.of(date, leave);

            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                long hours = Duration.between(date.atTime(9, 0), out).toHours();
                weekendAmt += baseHourly * hours * 2;
            }
        }
        return weekendAmt;
    }

    private int calculateTripPay(List<Attendance> records) {
        int tripAmt = 0;
        int dailyAllowance = 50000;
        for (Attendance att : records) {
            if ("X".equals(att.getAttStatus())) {
                tripAmt += dailyAllowance;
            }
        }
        return tripAmt;
    }
}
