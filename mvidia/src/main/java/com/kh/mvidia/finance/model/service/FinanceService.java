package com.kh.mvidia.finance.model.service;

import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Sales;
import com.kh.mvidia.finance.model.vo.Tax;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

public interface FinanceService {
    List<Salary> getSalaryByMonth(String yearMonth);

    List<Salary> getSalaryByCondition(Map<String, Object> param);

    Salary getSalaryByEmpAndMonth(String empNo, String payDate);

    List<Tax> getTaxesByEmpAndMonth(String empNo, String payDate);

    List<Sales> getQuarterlySales(String year);

    long getYearlySales(String year);

    int mergeQuarterlySales(String year);
}
