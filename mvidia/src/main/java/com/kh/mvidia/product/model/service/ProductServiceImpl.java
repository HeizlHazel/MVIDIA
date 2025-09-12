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
    public int selectListCount() { return pDao.selectListCount(sqlSession); }

    @Override
    public List<ProductQuality> selectList(PageInfo pi) { return pDao.selectList(sqlSession, pi);}

    @Override
    public List<ProductQuality> selectAllList(){ return pDao.selectAllList(sqlSession); }

}
