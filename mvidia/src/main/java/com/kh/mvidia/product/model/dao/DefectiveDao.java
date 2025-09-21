package com.kh.mvidia.product.model.dao;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.product.model.vo.DefectiveProduction;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Repository
public class DefectiveDao {

    public int selectListCount(SqlSessionTemplate sqlSession){

        return sqlSession.selectOne("defectiveMapper.selectListCount");
    }

    // 리스트 조회
    public ArrayList<DefectiveProduction> selectList(SqlSessionTemplate sqlSession, PageInfo dpi){

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

    // 불량 제품 삭제
    public int deletelist(SqlSessionTemplate sqlSession, ArrayList<String> defNoList){
        return sqlSession.update("defectiveMapper.deleteDefective", defNoList);
    }

    // 검색 조건 개수
    public int selectSearchCount(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        return sqlSession.selectOne("defectiveMapper.selectSearchCount", params);
    }

    // 검색 조건에 맞는 리스트 조회
    public ArrayList<DefectiveProduction> selectSearchList(SqlSessionTemplate sqlSession, Map<String, Object> params, PageInfo pi) {
        int offset = (pi.getCurrentPage() - 1) * pi.getBoardLimit();
        int limit = pi.getBoardLimit();
        RowBounds rowBounds = new RowBounds(offset, limit);

        return new ArrayList<>(sqlSession.selectList("defectiveMapper.selectSearchList", params, rowBounds));
    }


}
