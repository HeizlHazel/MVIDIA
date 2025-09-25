package com.kh.mvidia.employee.model.service;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.common.model.vo.Department;
import com.kh.mvidia.common.model.vo.EmpModifyReq;
import com.kh.mvidia.employee.model.dao.EmployeeDao;
import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public Employee selectEmpNo(String empNo) {
		return empDao.selectEmpNo(sqlSession,empNo);
	}
	
	@Override
	@Transactional
	public int insertEmpModifyRequests(List<EmpModifyReq> reqList){
			return empDao.insertEmpModifyRequests(sqlSession, reqList);
	}
	
	@Override
	public ArrayList<EmpModifyReq> selectReq(String empNo) {
		return empDao.selectReq(sqlSession, empNo);
	}
	
	@Override
	public int updateEmpModifyReqStatus(Map<String, Object> params) {
		return empDao.updateEmpModifyReqStatus(sqlSession,params);
	}
	
	@Override
	public int updateFile(Attachment atch) {
		return empDao.updateFile(sqlSession, atch);
	}
	
	@Override
	public EmpModifyReq findEmpModifyReqById(String reqId) {
		return empDao.findEmpModifyReqById(sqlSession, reqId);
	}
	
	@Override
	public int updateEmpSelective(Employee patch) {
		return empDao.updateEmpSelective(sqlSession, patch);
	}
	
	@Override
	public Attachment selectProfile(String empNo) {
		return empDao.selectProfile(sqlSession, empNo);
	}
	
	@Override
	public int deleteEmp(String empNo) {
		return empDao.deleteEmp(sqlSession, empNo);
	}
	
	@Override
	public ArrayList<Department> selectDeptList() {
		return empDao.selectDeptList(sqlSession);
	}
	
	@Override
	public ArrayList<Employee> selectEmpAllList() {
		return empDao.selectEmpAllList(sqlSession);
	}
	
	@Override
	public ArrayList<Employee> selectEmpByDept(String deptName) {
		return empDao.selectEmpByDept(sqlSession, deptName);
	}
	
	@Override
	public Employee checkEmpNo(String empNo) {
		return empDao.checkEmpNo(sqlSession, empNo);
	}
	
	@Override
	public Employee checkEmpNoCer(String empNo) {
		return empDao.checkEmpNoCer(sqlSession, empNo);
	}
	
	@Override
	public boolean checkPhone(String phone) {
		Employee emp =  empDao.checkPhone(sqlSession, phone);
		boolean exists = false;
		if(emp != null){
			exists = true;
		}
		return exists;
	}
	
	@Override
	public int demoteAllToLByEmp(String empNo) {
		return empDao.demoteAllToLByEmp(sqlSession, empNo);
		
	}
	
	@Override
	public Attachment selectModifyProfile(String empNo) {
		return empDao.selectModifyProfile(sqlSession, empNo);
	}
	
}
