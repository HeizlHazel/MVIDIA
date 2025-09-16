package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.integratedAtt.model.vo.Vacation;

import java.util.ArrayList;

public interface VacationService {

	ArrayList<Vacation> selectRecentVacations();
	
	ArrayList<Vacation> selectVacationList();
}
