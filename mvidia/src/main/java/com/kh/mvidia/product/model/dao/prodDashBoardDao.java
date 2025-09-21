package com.kh.mvidia.product.model.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class prodDashBoardDao {

    @Autowired
    private SqlSession sqlSession; // MyBatis SqlSession 주입

    private static final String NAMESPACE = "prodboardMapper."; // mapper.xml namespace

    public List<Map<String,Object>> getRecentDefective() {
        return sqlSession.selectList(NAMESPACE + "selectRecentDefective");
    }

    public Map<String,Object> getSummary() {
        return sqlSession.selectOne(NAMESPACE + "selectSummary");
    }

    public List<Map<String,Object>> getTop5Prog() {
        return sqlSession.selectList(NAMESPACE + "selectTop5Prog");
    }



}
