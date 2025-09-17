package com.kh.mvidia.integratedAtt.model.dao;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.integratedAtt.model.vo.Vacation;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Repository
public class VacationDao {
	public ArrayList<Vacation> selectRecentVacations(SqlSessionTemplate sqlSession) {
		return (ArrayList)sqlSession.selectList("vacationMapper.selectRecentVacations");
	}
	
	public int selectVaListCount(SqlSessionTemplate sqlSession, HashMap<String, String> searchMap) {
		return sqlSession.selectOne("vacationMapper.selectVaListCount",searchMap);
	}
	
	public ArrayList<Vacation> selectVacationList(SqlSessionTemplate sqlSession, HashMap<String, Object> paramMap) {
		return (ArrayList)sqlSession.selectList("vacationMapper.selectVacationList", paramMap);
	}
	
	
	public int updateVacation(SqlSessionTemplate sqlSession, Vacation va) {
		return sqlSession.update("vacationMapper.updateVacation", va);
	}
	
	public int insertVacation(SqlSessionTemplate sqlSession, Vacation va) {
		return sqlSession.insert("vacationMapper.insertVacation", va);
	}
	
	public ArrayList<Vacation> selectEmpVacationList(SqlSessionTemplate sqlSession, HashMap<String, Object> paramMap) {
		return (ArrayList)sqlSession.selectList("vacationMapper.selectEmpVacationList",paramMap);
	}
}
