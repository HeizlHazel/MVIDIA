package com.kh.mvidia.Permission.model.service;

import com.kh.mvidia.Permission.model.dao.PermissionDao;
import com.kh.mvidia.employee.model.vo.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionDao pDao;

    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public Employee selectEmp(String empNo) {
        return pDao.selectEmp(empNo);
    }

    @Override
    public void updatePermission(String userId, String permCode, String isGranted) {

    }
}
