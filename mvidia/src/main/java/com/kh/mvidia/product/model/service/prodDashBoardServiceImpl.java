package com.kh.mvidia.product.model.service;

import com.kh.mvidia.product.model.dao.prodDashBoardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class prodDashBoardServiceImpl implements prodDashBoardService{

    @Autowired
    private prodDashBoardDao prodDashboardDao;

    @Override
    public List<Map<String, Object>> getRecentDefective() {
        return prodDashboardDao.getRecentDefective();
    }

    @Override
    public Map<String, Object> getSummary() {
        return prodDashboardDao.getSummary();
    }

    @Override
    public List<Map<String, Object>> getTop5Prog() {
        return prodDashboardDao.getTop5Prog();
    }

}
