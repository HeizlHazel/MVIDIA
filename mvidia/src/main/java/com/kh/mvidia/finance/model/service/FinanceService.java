package com.kh.mvidia.finance.model.service;

import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public interface FinanceService {
    List<Salary> getSalaryByMonth(String yearMonth);
}
