package com.kh.mvidia.product.model.dao;

import com.kh.mvidia.common.model.vo.DefPageInfo;
import com.kh.mvidia.product.model.vo.DefectiveProduction;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public class DefectiveDao {

    public int selectListCount(SqlSessionTemplate sqlSession){

        return sqlSession.selectOne("defectiveMapper.selectListCount");
    }

    // 리스트 조회
    public ArrayList<DefectiveProduction> selectList(SqlSessionTemplate sqlSession, DefPageInfo dpi){

        int offset = (dpi.getCurrentPage() -1) * dpi.getBoardLimit();
        int limit = dpi.getBoardLimit();

        RowBounds rowBounds = new RowBounds(offset, limit);

        //return (ArrayList)sqlSession.selectList("defectiveMapper.selectList", null, rowBounds);
        return new ArrayList<>(sqlSession.selectList("defectiveMapper.selectList", null, rowBounds));
    }

    // 불량 제품 등록
    public int insertList(SqlSessionTemplate sqlSession, DefectiveProduction dp){
        return sqlSession.insert("defectiveMapper.insertDefective", dp);
    }

}
