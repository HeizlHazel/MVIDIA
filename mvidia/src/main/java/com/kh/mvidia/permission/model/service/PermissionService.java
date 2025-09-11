package com.kh.mvidia.permission.model.service;

import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.EmpPermission;
import com.kh.mvidia.permission.model.vo.Permission;

import java.util.List;

public interface PermissionService {

    // 사용자 검색
    Employee selectEmp(String empNo);

    // 전체 권한 목록 조회
    List<Permission> selectPermList();

    // 사용자 기존 권한 조회
    List<EmpPermission> selectEmpPermList(String empNo);

    // 권한 업데이트
    // void updatePermission(String userId, String permCode, String isGranted);

}
