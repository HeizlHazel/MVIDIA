package com.kh.mvidia.sales.model.service;

import com.kh.mvidia.sales.model.vo.Sales;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface SalesService {
    List<Sales> getQuarterlySales(String year);
    long getYearlySales(String year);
    void mergeQuarterlySales(String year);
    Map<String, Long> getQuarterlyProductRevenue(String year, int quarter);
    Map<String, Object> getQuarterlySummary(String year, int quarter);

}
