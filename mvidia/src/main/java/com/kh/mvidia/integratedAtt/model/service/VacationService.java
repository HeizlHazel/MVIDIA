package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.integratedAtt.model.vo.Vacation;

import java.util.ArrayList;
import java.util.HashMap;

public interface VacationService {

	ArrayList<Vacation> selectRecentVacations();
	
	int selectVaListCount(HashMap<String, String> searchMap);
	
	ArrayList<Vacation> selectVacationList(PageInfo pi, HashMap<String, String> searchMap);
	
	int updateVacation(Vacation va);
}
