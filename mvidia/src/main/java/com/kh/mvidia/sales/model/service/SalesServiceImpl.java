package com.kh.mvidia.sales.model.service;

import com.kh.mvidia.sales.model.dao.SalesDao;
import com.kh.mvidia.sales.model.vo.Sales;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public int mergeQuarterlySales(String year) {
        return SalesDao.mergeQuarterlySales(sqlSession, year);
    }
}
