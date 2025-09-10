package com.kh.mvidia.product.model.service;

import com.kh.mvidia.common.model.vo.DefPageInfo;
import com.kh.mvidia.product.model.dao.DefectiveDao;
import com.kh.mvidia.product.model.vo.DefectiveProduction;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DefectiveServiceImpl implements DefectiveService{

    @Autowired
    private DefectiveDao dDao;

    @Autowired
    private SqlSessionTemplate sqlSession;


    @Override
    public int selectListCount(){
        return dDao.selectListCount(sqlSession);
    }

    @Override
    public ArrayList<DefectiveProduction> selectList(DefPageInfo dpi){
        return dDao.selectList(sqlSession, dpi);
    }

    @Override
    public int insertDefective(DefectiveProduction dp){ return dDao.insertList(sqlSession, dp);}
}
