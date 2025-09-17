package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.integratedAtt.model.dao.AttendanceDao;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

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
	
	@Override
	public int selectAttListCount(HashMap<String, String> searchMap) {
		return attDao.selectAttListCount(sqlSession,searchMap);
	}
	
	@Override
	public ArrayList<Attendance> selectAttendanceList(PageInfo pi, HashMap<String, String> searchMap) {
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pi", pi);
		paramMap.put("searchMap", searchMap);
		return attDao.selectAttendanceList(sqlSession,paramMap);
	}
	
	@Override
	public int updateAttendance(Attendance att) {
		return attDao.updateAttendance(sqlSession,att);
	}
	
	@Override
	public ArrayList<Attendance> selectEmpAttendanceList(PageInfo pi, HashMap<String, String> searchMap){
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pi",pi);
		paramMap.put("searchMap", searchMap);
		return attDao.selectEmpAttendanceList(sqlSession,paramMap);
	}
	

}
