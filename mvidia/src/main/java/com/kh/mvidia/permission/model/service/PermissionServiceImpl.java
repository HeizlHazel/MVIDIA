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

    @Override
    public void updatePermission(String empNo, List<String> list) {
        // 1. 모든 권한 기본값 N 처리
        List<Permission> allPerms = pDao.selectPermList(sqlSession, empNo);
        for(Permission p : allPerms) {
            EmpPermission ep = new EmpPermission();
            ep.setUserId(empNo);
            ep.setPermCode(p.getPermCode());
            ep.setIsGranted("N"); // 초기화
            pDao.updateEmpPermission(sqlSession, ep);
            pDao.insertEmpPermission(sqlSession, ep);
        }

        // 2. 체크된 권한 Y로 업데이트
        if(list != null && !list.isEmpty()) {
            for(String pCode : list) {
                EmpPermission ep = new EmpPermission();
                ep.setUserId(empNo);
                ep.setPermCode(pCode);
                ep.setIsGranted("Y");
                pDao.updateEmpPermission(sqlSession, ep);
                pDao.insertEmpPermission(sqlSession, ep);
            }
        }
    }

}
