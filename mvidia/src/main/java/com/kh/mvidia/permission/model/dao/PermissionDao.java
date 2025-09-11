package com.kh.mvidia.permission.model.dao;

import com.kh.mvidia.permission.model.vo.EmpPermission;
import com.kh.mvidia.permission.model.vo.Permission;
import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PermissionDao {

    // 사용자 검색
    public Employee selectEmp(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectOne("permissionMapper.selectEmp", empNo);
    }

    // 전체 권한 목록 조회
    public List<Permission> selectPermList(SqlSessionTemplate sqlSession) {
        return sqlSession.selectList("permissionMapper.selectPermList");
    }

    // 사용자 기존 권한 조회
    public List<EmpPermission> selectEmpPermList(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectList("permissionMapper.selectEmpPermList", empNo);
    }

}
