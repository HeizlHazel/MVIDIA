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
    public Employee selectEmployee(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectOne("permissionMapper.selectEmployee", empNo);
    }

    // 전체 권한 목록 + 사용자 기존 권한 조회
    public List<Permission> selectPermList(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectList("permissionMapper.selectPermissionList", empNo);
    }

//    // 권한 부여
//    public int insertEmpPermission(SqlSessionTemplate sqlSession, EmpPermission ep) {
//        return sqlSession.insert("permissionMapper.insertEmpPermission", ep);
//    }
//
//    // 권한 회수
//    public int deleteEmpPermission(SqlSessionTemplate sqlSession, String userId) {
//        return sqlSession.delete("permissionMapper.deleteEmpPermission", userId);
//    }
}
