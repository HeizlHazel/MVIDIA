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
	
	public ArrayList<Attendance> selectEmpAttendanceList(SqlSessionTemplate sqlSession, HashMap<String, Object> paramMap){
		return (ArrayList)sqlSession.selectList("attendanceMapper.selectEmpAttendanceList",paramMap);
	}

    public Attendance selectToday(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectOne("attendanceMapper.selectToday", empNo);
    }

    public int checkInUpsert(SqlSessionTemplate sqlSession, HashMap<String, Object> param) {
        return sqlSession.insert("attendanceMapper.checkInUpsert", param);
    }

    public int checkOut(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.update("attendanceMapper.checkOut", empNo);
    }
	
	public int updateStatusToday(SqlSessionTemplate sqlSession, HashMap<String,Object> param) {
		return sqlSession.update("attendanceMapper.updateStatusToday", param);
	}
	
    public Attendance selectTodayTimes(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectOne("attendanceMapper.selectTodayTimes", empNo);
    }
}
