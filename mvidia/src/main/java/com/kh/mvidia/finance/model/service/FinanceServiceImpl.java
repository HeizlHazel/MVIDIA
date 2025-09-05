package com.kh.mvidia.finance.model.service;
import com.kh.mvidia.finance.model.dao.FinanceDao;
import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private FinanceDao financeDao;

    @Override
    public List<Salary> getSalaryByMonth(String yearMonth) {
        List<Salary> salaryList = financeDao.selectSalaryByMonth(sqlSession, yearMonth);

        for (Salary s : salaryList) {
            int baseSalary = Integer.parseInt(s.getSalary());
            int bonusPercent = Integer.parseInt(s.getBonus());

            int baseHourly = baseSalary / 209;

            List<Attendance> records =
                    financeDao.selectAttendanceByEmpMonth(sqlSession, s.getEmpNo() ,yearMonth);


            int extendedAmt = calculateExtendedPay(baseHourly, records);
            int nightAmt = calculateNightPay(baseHourly, records);
            int weekendAmt = calculateWeekendPay(baseHourly, records);
            int tripAmt = calculateTripPay(records);

            financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0001", extendedAmt);
            financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0002", nightAmt);
            financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0003", weekendAmt);
            financeDao.upsertSalaryOver(sqlSession, s.getEmpNo(), yearMonth, "OV0004", tripAmt);

// 총합
            int totalOv = extendedAmt + nightAmt + weekendAmt + tripAmt;
            financeDao.updateOvPrice(sqlSession, s.getEmpNo(), yearMonth, totalOv);
            s.setOvPrice(String.valueOf(totalOv));

            int bonusAmt = baseSalary * bonusPercent / 100;
            s.setBonus(String.valueOf(bonusAmt));

            int totalPay = baseSalary + totalOv + bonusAmt;
            System.out.println("총액(" + s.getEmpNo() + "): " + totalPay);
        }

        return salaryList;
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



}
