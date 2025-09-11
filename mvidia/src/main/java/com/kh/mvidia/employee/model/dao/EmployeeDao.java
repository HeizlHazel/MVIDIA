package com.kh.mvidia.employee.model.dao;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.common.model.vo.EmpModifyReq;
import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeDao {
	public Employee loginEmp(SqlSessionTemplate sqlSession, Employee emp) {
		return sqlSession.selectOne("employeeMapper.loginEmp", emp);
	}
	
	public int insertFile(SqlSessionTemplate sqlSession, Attachment atch) {
		return sqlSession.insert("AttachmentMapper.insertFile", atch);
	}
	
	public int insertEmpInfo(SqlSessionTemplate sqlSession, Employee emp) {
		return sqlSession.insert("employeeMapper.insertEmpInfo", emp);
	}
	
	public Attachment selectProfile(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.selectOne("AttachmentMapper.selectProfile", empNo);
	}
	
	public Employee selectEmpNo(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.selectOne("employeeMapper.selectEmpNo", empNo);
	}
	
	public int insertEmpModifyRequests(SqlSessionTemplate sqlSession, List<EmpModifyReq> reqList) {
		return sqlSession.insert("empModifyReqMapper.insertEmpModifyRequests", reqList);
	}
	
	public Employee checkEmpNo(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.selectOne("employeeMapper.checkEmpNo", empNo);
	}
}


