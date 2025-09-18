package com.kh.mvidia.product.model.service;

import com.kh.mvidia.product.model.dao.ChartDao;
import com.kh.mvidia.product.model.vo.ProgressChart;
import com.kh.mvidia.product.model.vo.ScheduleRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChartServiceImpl implements ChartService{

    @Autowired
    private ChartDao cDao;

    @Override
    public List<ScheduleRegistration> selectSchrList(){ return cDao.selectSchrList(); }

    @Override
    public List<ProgressChart> selectProgList(){ return cDao.selectProgList(); }

    @Override
    public  List<ScheduleRegistration> selectTop5Schr(){ return cDao.selectTop5Schr(); }

    @Override
    public List<ScheduleRegistration> selectTop5SchrDonut(){ return cDao.selectTop5SchrDonut(); }

    @Override
    public List<ProgressChart> selectTop5Prog(){ return cDao.selectTop5Prog(); }

    @Override
    public List<ScheduleRegistration> selectAllSchr(){ return  cDao.selectAllSchr(); }

    @Override
    public List<ScheduleRegistration> selectAllSchrDonut(){ return cDao.selectAllSchrDonut(); }


}
