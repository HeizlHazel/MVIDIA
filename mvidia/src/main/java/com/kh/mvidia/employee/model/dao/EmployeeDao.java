package com.kh.mvidia.employee.model.dao;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.common.model.vo.Department;
import com.kh.mvidia.common.model.vo.EmpModifyReq;
import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	public int updateEmpModifyReqStatus(SqlSessionTemplate sqlSession, Map<String, Object> params) {
		return sqlSession.update("empModifyReqMapper.updateEmpModifyReqStatus", params);
	}
	
	public int updateFile(SqlSessionTemplate sqlSession, Attachment atch) {
		return sqlSession.update("AttachmentMapper.updateFile", atch);
	}
	
	public EmpModifyReq findEmpModifyReqById(SqlSessionTemplate sqlSession, String reqId) {
		return sqlSession.selectOne("empModifyReqMapper.findEmpModifyReqById", reqId);
	}
	
	public int updateEmpSelective(SqlSessionTemplate sqlSession, Employee patch) {
		return sqlSession.update("employeeMapper.updateEmpSelective", patch);
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
	
	public ArrayList<EmpModifyReq> selectReq(SqlSessionTemplate sqlSession, String empNo) {
		return (ArrayList)sqlSession.selectList("empModifyReqMapper.selectReq", empNo);
	}
	
	public int deleteEmp(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.update("employeeMapper.deleteEmp", empNo);
	}
	
	public ArrayList<Department> selectDeptList(SqlSessionTemplate sqlSession) {
		return (ArrayList)sqlSession.selectList("departmentMapper.selectDeptList");
	}
	
	public ArrayList<Employee> selectEmpAllList(SqlSessionTemplate sqlSession) {
		return (ArrayList)sqlSession.selectList("employeeMapper.selectEmpAllList");
	}
	
	public ArrayList<Employee> selectEmpByDept(SqlSessionTemplate sqlSession, String deptName) {
		return (ArrayList)sqlSession.selectList("employeeMapper.selectEmpByDept", deptName);
	}
	
	public Employee checkEmpNo(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.selectOne("employeeMapper.checkEmpNo", empNo);
	}
	
	
	public Employee checkEmpNoCer(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.selectOne("employeeMapper.checkEmpNoCer", empNo);
	}
	
	public Employee checkPhone(SqlSessionTemplate sqlSession, String phone) {
		return sqlSession.selectOne("employeeMapper.checkPhone", phone);
	}
	
	public int demoteAllToLByEmp(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.update("AttachmentMapper.demoteAllToLByEmp", empNo);
	}
	
	public Attachment selectModifyProfile(SqlSessionTemplate sqlSession, String empNo) {
		return sqlSession.selectOne("AttachmentMapper.selectModifyProfile", empNo);
	}
}


