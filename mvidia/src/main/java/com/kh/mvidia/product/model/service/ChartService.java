package com.kh.mvidia.product.model.service;

import com.kh.mvidia.product.model.vo.ProgressChart;
import com.kh.mvidia.product.model.vo.ScheduleRegistration;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChartService {

    List<ScheduleRegistration> selectSchrList();

    List<ProgressChart> selectProgList();

    List<ScheduleRegistration> selectTop5Schr();

    List<ScheduleRegistration> selectTop5SchrDonut();

    List<ProgressChart> selectTop5Prog();

    List<ScheduleRegistration> selectAllSchr(String bpPartner);

    List<ScheduleRegistration> selectAllSchrDonut(String bpPartner);
}
