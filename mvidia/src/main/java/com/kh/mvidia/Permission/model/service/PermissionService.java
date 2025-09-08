package com.kh.mvidia.Permission.model.service;

import com.kh.mvidia.Permission.model.vo.Employee;

import java.util.ArrayList;

public interface PermissionService {

    // 사원 검색
    Employee selectEmp(String empNo);

    // 권한 업데이트
    void updatePermission(String userId, String permCode, String isGranted);

}
