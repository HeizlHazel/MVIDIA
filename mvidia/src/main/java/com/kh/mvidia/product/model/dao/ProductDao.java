package com.kh.mvidia.product.model.dao;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.product.model.vo.ProductQuality;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDao {

    // 리스트 카운트 조회
    public int selectListCount(SqlSessionTemplate sqlSession){

        return sqlSession.selectOne("productMapper.selectListCount");
    }

    // 리스트 조회
    public List<ProductQuality> selectList(SqlSessionTemplate sqlSession, PageInfo dpi){

        int offset = (dpi.getCurrentPage() -1) * dpi.getBoardLimit();
        int limit = dpi.getBoardLimit();

        RowBounds rowBounds = new RowBounds(offset, limit);

        return sqlSession.selectList("productMapper.selectList", null, rowBounds);
    }

    // 불량 등록용 리스트 조회?
    public List<ProductQuality> selectAllList(SqlSessionTemplate sqlSession){
        return sqlSession.selectList("productMapper.selectAllList");
    }


}
