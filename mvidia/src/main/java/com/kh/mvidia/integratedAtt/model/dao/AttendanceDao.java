package com.kh.mvidia.integratedAtt.model.dao;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Repository
public class AttendanceDao {
	
	public ArrayList<Attendance> selectRecentAttendances(SqlSessionTemplate sqlSession) {
		return (ArrayList)sqlSession.selectList("attendanceMapper.selectRecentAttendances");
	}
	
	public int selectAttListCount(SqlSessionTemplate sqlSession, HashMap<String, String> searchMap) {
		return sqlSession.selectOne("attendanceMapper.selectAttListCount", searchMap);
	}
	
	public ArrayList<Attendance> selectAttendanceList(SqlSessionTemplate sqlSession, HashMap<String, Object> paramMap) {
		return (ArrayList)sqlSession.selectList("attendanceMapper.selectAttendanceList",paramMap);
	}
	
	
	public int updateAttendance(SqlSessionTemplate sqlSession, Attendance att) {
		return sqlSession.update("attendanceMapper.updateAttendance", att);
	}
}
