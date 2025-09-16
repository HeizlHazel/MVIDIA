package com.kh.mvidia.integratedAtt.model.dao;

import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public class AttendanceDao {
	
	public ArrayList<Attendance> selectRecentAttendances(SqlSessionTemplate sqlSession) {
		return (ArrayList)sqlSession.selectList("attendanceMapper.selectRecentAttendances");
	}
}
