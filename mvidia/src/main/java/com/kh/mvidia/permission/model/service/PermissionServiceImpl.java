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
    public Employee selectEmp(String empNo) {
        return pDao.selectEmp(sqlSession, empNo);
    }

    // 전체 권한 목록 조회
    @Override
    public List<Permission> selectPermList() { return pDao.selectPermList(sqlSession); }

    // 사용자 기존 권한 조회
    @Override
    public List<EmpPermission> selectEmpPermList(String empNo) { return pDao.selectEmpPermList(sqlSession, empNo); }

    // 권한 업데이트
    // @Override
    // public void updatePermission(String userId, String permCode, String isGranted) {}

}
