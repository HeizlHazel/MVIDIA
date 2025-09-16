package com.kh.mvidia.sales.model.service;

import com.kh.mvidia.sales.model.vo.Sales;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SalesService {
    List<Sales> getQuarterlySales(String year);
    long getYearlySales(String year);
    int mergeQuarterlySales(String year);
}
