package com.kh.mvidia.permission.model.service;

import com.kh.mvidia.permission.model.dao.PermissionDao;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.EmpPermission;
import com.kh.mvidia.permission.model.vo.Permission;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionDao pDao;

    @Autowired
    private SqlSessionTemplate sqlSession;

    // 사용자 검색
    @Override
    public Employee selectEmployee(String empNo) {
        return pDao.selectEmployee(sqlSession, empNo);
    }

    // 전체 권한 목록 + 사용자 기존 권한 조회
    @Override
    public List<Permission> selectPermissionList(String empNo) {
        return pDao.selectPermList(sqlSession, empNo);
    }

//    // 권한 update
//    @Override
//    public void updatePermission(String userId, List<String> list) {
//    }

//    // 권한 부여
//    @Override
//    public int grantPermission(EmpPermission ep) {
//        return pDao.insertEmpPermission(sqlSession, ep);
//    }
//
//    // 권한 회수
//    @Override
//    public int revokePermission(EmpPermission ep) {
//        return pDao.deleteEmpPermission(sqlSession, ep);
//    }


}
