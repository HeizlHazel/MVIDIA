package com.kh.mvidia.product.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
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
    public ArrayList<DefectiveProduction> selectList(PageInfo pi){
        return dDao.selectList(sqlSession, pi);
    }

    @Override
    public int insertDefective(DefectiveProduction dp){ return dDao.insertList(sqlSession, dp);}

    @Override
    public int deleteDefective(ArrayList<String> defNoList){ return dDao.deletelist(sqlSession, defNoList);}
}
