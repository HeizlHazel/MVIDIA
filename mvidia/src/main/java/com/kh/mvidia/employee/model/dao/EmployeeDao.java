package com.kh.mvidia.employee.model.dao;

import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDao {
	public Employee loginEmp(SqlSessionTemplate sqlSession, Employee emp) {
		return sqlSession.selectOne("employeeMapper.loginEmp", emp);
	}
}
