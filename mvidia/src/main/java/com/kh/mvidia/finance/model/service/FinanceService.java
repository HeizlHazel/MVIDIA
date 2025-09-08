package com.kh.mvidia.finance.model.service;

import com.kh.mvidia.finance.model.vo.Salary;

import java.util.List;
import java.util.Map;

public interface FinanceService {
    List<Salary> getSalaryByMonth(String yearMonth);

    List<Salary> getSalaryByCondition(Map<String, Object> param);
}
