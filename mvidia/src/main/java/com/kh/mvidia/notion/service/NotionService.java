package com.kh.mvidia.notion.service;

import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;

import java.util.List;

public interface NotionService {
    void insertPayrollToNotion(Salary salary, List<Tax> taxList);

}
