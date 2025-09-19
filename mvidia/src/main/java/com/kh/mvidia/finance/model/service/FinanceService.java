package com.kh.mvidia.finance.model.service;

import com.kh.mvidia.finance.model.vo.Comp;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

public interface FinanceService {
    List<Salary> getSalary(Map<String, Object> param);

    Salary getSalaryByEmpAndMonth(String empNo, String payDate);

    List<Tax> getTaxesByEmpAndMonth(String empNo, String payDate);

    List<Comp> getAllComponents();

    List<Salary> getFilteredSalary(String yearMonth, String deptCode, String jobCode, String keyword);

    List<Comp> searchComponents(String keyword, String localCode, String status);;

}
