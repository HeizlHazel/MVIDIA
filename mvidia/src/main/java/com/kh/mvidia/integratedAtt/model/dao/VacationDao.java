package com.kh.mvidia.integratedAtt.model.dao;

import com.kh.mvidia.integratedAtt.model.vo.Vacation;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public class VacationDao {
	public ArrayList<Vacation> selectRecentVacations(SqlSessionTemplate sqlSession) {
		return (ArrayList)sqlSession.selectList("vacationMapper.selectRecentVacations");
	}
	
	public ArrayList<Vacation> selectVacationList(SqlSessionTemplate sqlSession) {
		return (ArrayList)sqlSession.selectList("vacationMapper.selectVacationList");
	}
}
