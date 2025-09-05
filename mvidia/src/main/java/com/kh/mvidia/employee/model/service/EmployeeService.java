package com.kh.mvidia.employee.model.service;


import com.kh.mvidia.employee.model.vo.Employee;

public interface EmployeeService {

	// 로그인 서비스
	Employee loginEmp(Employee emp);
	
	// 사원 생성 서비스
	int insertEmp(Employee emp);
	
	// 사원 수정 서비스
	int updateEmp(Employee emp);
	
	// 사원 삭제 서비스
	int deleteEmp(String empNo);
	
	// 사번 확인 서비스
	int EmpNoCheck(String employeeId);
	
}
