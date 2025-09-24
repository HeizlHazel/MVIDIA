package com.kh.mvidia.employee.model.service;


import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.common.model.vo.Department;
import com.kh.mvidia.common.model.vo.EmpModifyReq;
import com.kh.mvidia.employee.model.vo.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface EmployeeService {

	// 로그인 서비스
	Employee loginEmp(Employee emp);
	
	// 사원 생성 서비스
	int insertFile(Attachment atch);
	
	int insertEmpInfo(Employee emp);
	
	// 사원 수정 서비스
	Employee selectEmpNo(String empNo);
	
	int insertEmpModifyRequests(List<EmpModifyReq> reqList);
	
	ArrayList<EmpModifyReq> selectReq(String empNo);
	
	int updateEmpModifyReqStatus(Map<String, Object> params);
	
	int updateFile(Attachment atch);
	
	EmpModifyReq findEmpModifyReqById(String reqId);
	
	int updateEmpSelective(Employee patch);
	
	Attachment selectProfile(String empNo);
	
	// 사원 삭제 서비스
	int deleteEmp(String empNo);
	
	ArrayList<Department> selectDeptList();
	
	ArrayList<Employee> selectEmpAllList();
	
	ArrayList<Employee> selectEmpByDept(String deptName);
	
	// 사번 확인 서비스
	Employee checkEmpNo(String empNo);
	
	Employee checkEmpNoCer(String empNo);
	
	boolean checkPhone(String phone);
	
}
