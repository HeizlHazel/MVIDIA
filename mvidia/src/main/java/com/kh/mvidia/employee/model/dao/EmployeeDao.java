package com.kh.mvidia.employee.model.dao;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

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
	
	public Employee checkEmpNo(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.selectOne("employeeMapper.checkEmpNo", empNo);
	}
}
