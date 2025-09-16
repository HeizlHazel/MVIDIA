package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.integratedAtt.model.dao.VacationDao;
import com.kh.mvidia.integratedAtt.model.vo.Vacation;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class VacationServiceImpl implements VacationService{
	
	@Autowired
	private VacationDao vaDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Override
	public ArrayList<Vacation> selectRecentVacations() {
		return vaDao.selectRecentVacations(sqlSession);
	}
	
	@Override
	public ArrayList<Vacation> selectVacationList() {
		return vaDao.selectVacationList(sqlSession);
	}
}
