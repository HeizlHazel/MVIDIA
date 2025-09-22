package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;

import java.util.ArrayList;
import java.util.HashMap;

public interface AttendanceService {
	
	ArrayList<Attendance> selectRecentAttendances();
	
	int selectAttListCount(HashMap<String, String> searchMap);
	
	ArrayList<Attendance> selectAttendanceList(PageInfo pi, HashMap<String, String> searchMap);
	
	int updateAttendance(Attendance att);
	
	ArrayList<Attendance> selectEmpAttendanceList(PageInfo pi, HashMap<String, String> searchMap);

    Attendance selectToday(String empNo);

    public HashMap<String,Object> checkInUpsert(String empNo);

    public HashMap<String,Object> checkOut(String empNo);

    public Attendance selectTodayTimes(String empNo);
}
