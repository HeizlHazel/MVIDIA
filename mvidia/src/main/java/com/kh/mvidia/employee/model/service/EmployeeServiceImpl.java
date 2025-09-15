package com.kh.mvidia.employee.model.service;

import com.kh.mvidia.common.model.vo.Attachment;
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
	public Employee checkEmpNo(String empNo) {
		return empDao.checkEmpNo(sqlSession, empNo);
	}
}
