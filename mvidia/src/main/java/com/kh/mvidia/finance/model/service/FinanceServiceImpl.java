package com.kh.mvidia.finance.model.service;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.kh.mvidia.finance.model.dao.FinanceDao;
import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private FinanceDao financeDao;
    @Autowired
    private ParameterNamesModule parameterNamesModule;

    @Override
    public Salary getSalaryByEmpAndMonth(String empNo, String payDate) {
        System.out.println("🔍 [FinanceService] getSalaryByEmpAndMonth 호출");
        System.out.println("   - 입력 empNo: [" + empNo + "]");
        System.out.println("   - 입력 payDate: [" + payDate + "]");

        try {
            Map<String, Object> param = new HashMap<>();
            param.put("empNo", empNo);
            param.put("yearMonth", payDate);
            Salary result = financeDao.selectSalaryByEmpAndMonth(sqlSession, param);

            System.out.println("   - 쿼리 실행 결과: " + (result != null ? "데이터 존재" : "데이터 없음"));

            if (result != null) {
                System.out.println("   - 조회된 데이터:");
                System.out.println("     * empNo: " + result.getEmpNo());
                System.out.println("     * empName: " + result.getEmpName());
                System.out.println("     * payDate: " + result.getPayDate());
            }

            return result;

        } catch (Exception e) {
            System.err.println("❌ [FinanceService] 쿼리 실행 중 오류:");
            e.printStackTrace();
            return null;
        }
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

            int extendedAmt = calculateExtendedPay(baseHourly, records, yearMonth);
            int nightAmt    = calculateNightPay(baseHourly, records, yearMonth);
            int weekendAmt  = calculateWeekendPay(baseHourly, records, yearMonth);
            int tripAmt     = calculateTripPay(records, yearMonth);

            upsertOvertimes(s, yearMonth , extendedAmt, nightAmt, weekendAmt, tripAmt);

            int totalOv = extendedAmt + nightAmt + weekendAmt + tripAmt;
            financeDao.updateOvPrice(sqlSession, s.getEmpNo(), yearMonth, totalOv);
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
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0001", taxNP); // 국민연금
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0002", taxHI); // 건강보험
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0003", taxUE); // 고용보험
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0004", taxIC); // 소득세
            financeDao.upsertSalaryTax(sqlSession, s.getEmpNo(), yearMonth, "TAX0005", taxLS); // 지방소득세

            financeDao.updateDeductAmt(sqlSession, s.getEmpNo(), yearMonth, deductAmt);
            System.out.println("세금 반영: " + s.getEmpNo() + ", " + yearMonth
                    + " NP=" + taxNP + " HI=" + taxHI + " UE=" + taxUE + " IC=" + taxIC + " LS=" + taxLS);
            System.out.println("👀 근태 조회: empNo=" + s.getEmpNo() + ", yearMonth=" + yearMonth + ", records=" + records.size());
            for (Attendance att : records) {
                System.out.println("   - attDate=" + att.getAttDate() +
                        ", leavingTime=" + att.getLeavingTime() +
                        ", status=" + att.getAttStatus());
            }



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

    private int calculateExtendedPay(int baseHourly, List<Attendance> records, String yearMonth) {
        int extendedAmt = 0;
        for (Attendance att : records) {
            if (att.getAttDate() == null || att.getLeavingTime() == null) continue;

            if (!att.getAttDate().startsWith(yearMonth)) continue;

            try{
                LocalDate date = LocalDate.parse(att.getAttDate());
                LocalTime leave = LocalTime.parse(att.getLeavingTime());
                LocalDateTime out = LocalDateTime.of(date, leave);

                DayOfWeek day = date.getDayOfWeek();
                if (day == DayOfWeek.SUNDAY ||
                        (day == DayOfWeek.MONDAY && out.isBefore(date.atTime(6,0)))) {
                    continue;
                }

                LocalDateTime sixPM = date.atTime(18, 0);
                LocalDateTime tenPM = date.atTime(22, 0);

                if (out.isAfter(sixPM)) {
                    LocalDateTime end = out.isBefore(tenPM) ? out : tenPM;
                    long hours = Duration.between(sixPM, end).toHours();
                    extendedAmt += (int) (baseHourly * hours);
                }
            } catch (Exception e) {
                System.err.println("ExtendedPay 파싱 오류: " + att);
            }

        }
        return extendedAmt;
    }

    private int calculateNightPay(int baseHourly, List<Attendance> records, String yearMonth) {
        int nightAmt = 0;
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate nextMonthFirst = ym.plusMonths(1).atDay(1);
        LocalDateTime nextMonthLimit = nextMonthFirst.atTime(6, 0);

        for (Attendance att : records) {
            if (att.getAttDate() == null || att.getLeavingTime() == null) continue;

            try {
                LocalDate date = LocalDate.parse(att.getAttDate());
                LocalTime leave = LocalTime.parse(att.getLeavingTime());
                LocalDateTime out = LocalDateTime.of(date, leave);

                DayOfWeek day = date.getDayOfWeek();
                if (day == DayOfWeek.SUNDAY ||
                        (day == DayOfWeek.MONDAY && out.isBefore(date.atTime(6,0)))) {
                    continue;
                }

                LocalDateTime tenPM = date.atTime(22, 0);
                LocalDateTime sixAM = date.plusDays(1).atTime(6, 0);

                if (out.isAfter(tenPM)) {
                    LocalDateTime end = out.isBefore(sixAM) ? out : sixAM;
                    long hours = Duration.between(tenPM, end).toHours();
                    nightAmt += (int) (baseHourly * hours * 1.5);
                }
            } catch (Exception e) {
                System.err.println("ExtendedPay 파싱 오류: " + att);
            }
        }
        return nightAmt;
    }

    private int calculateWeekendPay(int baseHourly, List<Attendance> records, String yearMonth) {
        int weekendAmt = 0;
            for (Attendance att : records) {
                if (att.getAttDate() == null || att.getLeavingTime() == null) continue;
                if (!att.getAttDate().startsWith(yearMonth)) continue;

                LocalDate date = LocalDate.parse(att.getAttDate());
                LocalTime leave = LocalTime.parse(att.getLeavingTime());
                LocalDateTime out = LocalDateTime.of(date, leave);

                DayOfWeek day = date.getDayOfWeek();
                if (day == DayOfWeek.SUNDAY) {
                    long hours = Duration.between(date.atTime(9, 0), out).toHours();
                    weekendAmt += (int) (baseHourly * hours * 1.5);
                }
            }
        return weekendAmt;
    }

    private int calculateTripPay(List<Attendance> records, String yearMonth) {
        int tripAmt = 0;
        int dailyAllowance = 50000;
        for (Attendance att : records) {
            if ("X".equals(att.getAttStatus())) {
                if (att.getAttDate() == null || att.getAttDate().startsWith(yearMonth)) {
                    tripAmt += dailyAllowance;
                }
            }
        }
        return tripAmt;
    }
}
