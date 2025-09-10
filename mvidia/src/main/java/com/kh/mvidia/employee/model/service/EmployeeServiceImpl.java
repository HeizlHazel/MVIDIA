package com.kh.mvidia.employee.model.service;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.employee.model.dao.EmployeeDao;
import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService{
	
	@Autowired
	private EmployeeDao empDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	@Override
	public Employee loginEmp(Employee emp) {
		return empDao.loginEmp(sqlSession, emp);
	}
	
	@Override
	public int insertFile(Attachment atch){
		return empDao.insertFile(sqlSession, atch);
	}
	
	@Override
	public int insertEmpInfo(Employee emp) {
		return empDao.insertEmpInfo(sqlSession, emp);
	}
	
	@Override
	public int updateFile(Attachment atch){
		return 0;
	}
	
	@Override
	public int updateEmp(Employee emp) {
		return 0;
	}
	
	@Override
	public Attachment selectProfile(String empNo) {
		return empDao.selectProfile(sqlSession, empNo);
	}
	
	@Override
	public int deleteEmp(String empNo) {
		return 0;
	}
	
	@Override
	public int EmpNoCheck(String employeeId) {
		return 0;
	}
}
