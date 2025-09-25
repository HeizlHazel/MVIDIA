package com.kh.mvidia.product.model.service;

import com.kh.mvidia.notion.dto.ScheduleSummaryDto;
import com.kh.mvidia.product.model.dao.ChartDao;
import com.kh.mvidia.product.model.vo.ProgressChart;
import com.kh.mvidia.product.model.vo.ScheduleRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChartServiceImpl implements ChartService{

    private final ChartDao cDao;

    @Autowired
    public ChartServiceImpl(ChartDao cDao) {
        this.cDao = cDao;
    }

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
    public List<ScheduleRegistration> selectAllSchr(String bpPartner){ return  cDao.selectAllSchr(bpPartner); }

    @Override
    public List<ScheduleRegistration> selectAllSchrDonut(String bpPartner){ return cDao.selectAllSchrDonut(bpPartner); }

    @Override
    public List<ScheduleSummaryDto> getScheduleDetail() {
        return cDao.selectScheduleDetail(); // ✅ 인스턴스로 호출
    }

    @Override
    public List<ScheduleSummaryDto> getCompanySummary() {
        return cDao.selectCompanySummary(); // ✅ 인스턴스로 호출
    }

}
