package com.kh.mvidia.Permission.model.dao;

import org.mybatis.spring.SqlSessionTemplate;
import com.kh.mvidia.employee.model.vo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
@Repository
public class PermissionDao {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public Employee selectEmp(String empNo) {
        return sqlSession.selectOne("permissionMapper.selectEmp", empNo);
    }
}
