package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.integratedAtt.model.vo.Attendance;

import java.util.ArrayList;

public interface AttendanceService {
	
	ArrayList<Attendance> selectRecentAttendances();
}
