package com.kh.mvidia.sales.model.service;

import com.kh.mvidia.sales.model.dao.SalesDao;
import com.kh.mvidia.sales.model.vo.Sales;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesServiceImpl implements SalesService {

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private SalesDao salesDao;

    @Override
    public List<Sales> getQuarterlySales(String year) {
        return salesDao.selectQuarterlySales(sqlSession, year);
    }

    @Override
    public long getYearlySales(String year) {
        List<Sales> list = getQuarterlySales(year); // 이미 있는 쿼터별 조회 활용
        long sum = 0;
        for (Sales s : list) {
            sum += Long.parseLong(s.getTotalSales());
        }
        return sum;
    }

    @Override
    public void mergeQuarterlySales(String year) {
        SalesDao.mergeQuarterlySales(sqlSession, year);
    }

    @Override
    public Map<String, Long> getQuarterlyProductRevenue(String year, int quarter) {
        List<Sales> salesList = getQuarterlySales(year);

        Map<String, Long> map = new HashMap<>();
        for (Sales s : salesList) {
            if (Integer.parseInt(s.getQuarter()) == quarter) {
                long sales = Long.parseLong(s.getTotalSales());
                map.merge(s.getProdName(), sales, Long::sum);
            }
        }
        return map;
    }
    @Override
    public Map<String, Object> getQuarterlySummary(String year, int quarter) {
        List<Sales> salesList = getQuarterlySales(year);

        long totalSales = 0;
        long totalProfit = 0;

        for (Sales s : salesList) {
            if (Integer.parseInt(s.getQuarter()) == quarter) {
                totalSales += Long.parseLong(s.getTotalSales());
                totalProfit += Long.parseLong(s.getOpProfit());
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSales", totalSales);
        summary.put("totalProfit", totalProfit);
        summary.put("profitRate", totalSales > 0 ? (double) totalProfit / totalSales * 100 : 0.0);

        return summary;
    }


}
