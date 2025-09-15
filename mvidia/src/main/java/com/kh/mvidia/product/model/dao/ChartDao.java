package com.kh.mvidia.product.model.dao;

import com.kh.mvidia.product.model.vo.ProgressChart;
import com.kh.mvidia.product.model.vo.ScheduleRegistration;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChartDao {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public List<ScheduleRegistration> selectSchrList(){
        return sqlSession.selectList("chartMapper.selectScheduleRegistration");
    }

    public List<ProgressChart> selectProgList(){
        return sqlSession.selectList("chartMapper.selectProgressChart");
    }

    public List<ScheduleRegistration> selectTop5Schr(){
        return  sqlSession.selectList("chartMapper.selectTop5Schr");
    }

    public List<ScheduleRegistration> selectAllSchr(){
        return sqlSession.selectList("chartMapper.selectAllSchr");
    }

    public List<ProgressChart> selectTop5Prog(){
        return sqlSession.selectList("chartMapper.selectTop5Prog");
    }

}
