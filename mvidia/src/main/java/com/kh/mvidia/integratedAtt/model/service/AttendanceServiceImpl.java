package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.integratedAtt.model.dao.AttendanceDao;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AttendanceServiceImpl implements AttendanceService{
	
	@Autowired
	private AttendanceDao attDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Override
	public ArrayList<Attendance> selectRecentAttendances() {
		return attDao.selectRecentAttendances(sqlSession);
	}
}
