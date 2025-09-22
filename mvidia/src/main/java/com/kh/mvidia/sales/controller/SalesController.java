package com.kh.mvidia.sales.controller;

import com.kh.mvidia.sales.model.service.SalesService;
import com.kh.mvidia.sales.model.vo.Sales;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/finance")
public class SalesController {

    @Autowired
    private SalesService salesService;

    @GetMapping("/revenue-report")
    public String revenueReport(
            @RequestParam(defaultValue = "2025") String year,
            Model model) {

        // 분기별 매출 데이터 병합 (업데이트 or insert)
        salesService.mergeQuarterlySales(year);

        // 최근 4개년 조회용
        List<String> yearList = Arrays.asList("2025", "2024", "2023", "2022");

        // 분기별 매출 조회
        List<Sales> salesList = salesService.getQuarterlySales(year);

        Map<String, long[]> productQuarterMap = new HashMap<>();
        Map<String, Object> productSumMap = new HashMap<>();

        long[] totalSales = new long[4];
        long[] totalProfits = new long[4];

        long sumSales = 0;
        long sumProfit = 0;

        for (Sales s : salesList) {
            int qIndex = Integer.parseInt(s.getQuarter()) - 1;

            long sales = Long.valueOf(s.getTotalSales());
            long profit = Long.valueOf(s.getOpProfit());

            productQuarterMap.putIfAbsent(s.getProdName(), new long[4]);
            productQuarterMap.get(s.getProdName())[qIndex] += sales;

            productSumMap.put(s.getProdName(),
                    ((long) productSumMap.getOrDefault(s.getProdName(), 0L)) + sales);

            totalSales[qIndex] += sales;
            totalProfits[qIndex] += profit;

            sumSales += sales;
            sumProfit += profit;
        }

        long ytd = sumSales;

        long prevYearSales = salesService.getYearlySales(String.valueOf(Integer.parseInt(year) - 1));

        double yoy = (prevYearSales > 0)
                ? ((double) (ytd - prevYearSales) / prevYearSales) * 100
                : 0.0;

        double profitRate = (ytd > 0) ? ((double) sumProfit / ytd) * 100 : 0.0;

        // 최대/최소 분기 찾기
        int maxSalesQ = 0, minSalesQ = 0;
        long maxSalesVal = totalSales[0], minSalesVal = totalSales[0];

        for (int i = 1; i < 4; i++) {
            if (totalSales[i] > maxSalesVal) {
                maxSalesVal = totalSales[i];
                maxSalesQ = i;
            }
            if (totalSales[i] < minSalesVal) {
                minSalesVal = totalSales[i];
                minSalesQ = i;
            }
        }

        // 모델에 데이터 세팅
        model.addAttribute("profitList", salesList);
        model.addAttribute("productQuarterMap", productQuarterMap);
        model.addAttribute("productSumMap", productSumMap);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalProfits", totalProfits);
        model.addAttribute("yearList", yearList);
        model.addAttribute("year", year);
        model.addAttribute("ytd", ytd / 100000000);

        model.addAttribute("yoy", String.format("%.1f", yoy));
        model.addAttribute("profitRate", String.format("%.1f", profitRate));
        model.addAttribute("maxSales", String.format("%d분기 : %,d억", maxSalesQ + 1, maxSalesVal / 100000000));
        model.addAttribute("minSales", String.format("%d분기 : %,d억", minSalesQ + 1, minSalesVal / 100000000));


        return "finance/revenue-report";
    }


}
