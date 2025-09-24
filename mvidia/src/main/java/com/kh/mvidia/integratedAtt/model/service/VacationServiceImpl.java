package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.integratedAtt.model.dao.VacationDao;
import com.kh.mvidia.integratedAtt.model.vo.Vacation;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

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
	public int selectVaListCount(HashMap<String, String> searchMap) {
		return vaDao.selectVaListCount(sqlSession,searchMap);
	}
	
	@Override
	public ArrayList<Vacation> selectVacationList(PageInfo pi, HashMap<String, String> searchMap) {
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pi", pi);
		paramMap.put("searchMap", searchMap);
		return vaDao.selectVacationList(sqlSession,paramMap);
	}
	
	@Override
	public int updateVacation(Vacation va) {
		return vaDao.updateVacation(sqlSession,va);
	}
	
	@Override
	public int insertVacation(Vacation va) {
		return vaDao.insertVacation(sqlSession,va);
	}
	
	@Override
	public ArrayList<Vacation> selectEmpVacationList(PageInfo pi, HashMap<String, String> searchMap) {
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pi", pi);
		paramMap.put("searchMap",searchMap);
		return vaDao.selectEmpVacationList(sqlSession, paramMap);
	}
	
	@Override
	public int selectEmpVaListCount(HashMap<String, String> searchMap) {
		return vaDao.selectEmpVaListCount(sqlSession,searchMap);
	}
	
}
