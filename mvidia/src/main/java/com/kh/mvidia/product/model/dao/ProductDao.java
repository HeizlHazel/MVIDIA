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
    public int selectListCount(SqlSessionTemplate sqlSession, String keyword){

        return sqlSession.selectOne("productMapper.selectListCount", keyword);
    }

    // 리스트 조회
    public List<ProductQuality> selectList(SqlSessionTemplate sqlSession, PageInfo pi, String keyword){

        int offset = (pi.getCurrentPage() -1) * pi.getBoardLimit();
        int limit = pi.getBoardLimit();

        RowBounds rowBounds = new RowBounds(offset, limit);

        return sqlSession.selectList("productMapper.selectList", keyword, rowBounds);
    }

    // 불량 등록용 리스트 조회?
    public List<ProductQuality> selectAllList(SqlSessionTemplate sqlSession){
        return sqlSession.selectList("productMapper.selectAllList");
    }


}
