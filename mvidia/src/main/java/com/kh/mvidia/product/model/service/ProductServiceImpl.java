package com.kh.mvidia.product.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.product.model.dao.ProductDao;
import com.kh.mvidia.product.model.vo.ProductQuality;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductDao pDao;

    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public int selectListCount(String keyword) { return pDao.selectListCount(sqlSession, keyword); }

    @Override
    public List<ProductQuality> selectList(PageInfo pi, String keyword) { return pDao.selectList(sqlSession, pi, keyword);}

    @Override
    public List<ProductQuality> selectAllList(){ return pDao.selectAllList(sqlSession); }

}
