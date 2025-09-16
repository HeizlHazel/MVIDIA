package com.kh.mvidia.sales.model.dao;

import com.kh.mvidia.sales.model.vo.Sales;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SalesDao {
    public long selectYearlySales(SqlSession sqlSession ,String year) {
        Long result = sqlSession.selectOne("salesMapper.selectYearlySales", year);
        return (result != null) ? result : 0;
    }

    public static List<Sales> selectQuarterlySales(SqlSession sqlSession, String year) {
        return sqlSession.selectList("salesMapper.selectQuarterlySales", year);
    }

    public static int mergeQuarterlySales(SqlSession sqlSession, String year) {
        return sqlSession.insert("salesMapper.mergeQuarterlySales", year);
    }
}
