package com.kh.mvidia.permission.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.Permission;
import com.kh.mvidia.permission.model.vo.SystemLog;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    // 사용자 검색
    Employee selectEmployee(String empNo);

    // 전체 권한 목록 + 사용자 기존 권한 조회
    List<Permission> selectPermissionList(String empNo);

    // 권한 update
    void updatePermission(String empNo, List<String> list);

    // 권한 가져와서 세션에 저장
    List<Permission> getUserGrantedPermissions(String empNo);

    // 권한 변경 이력 DB에 로그 저장
    void savePermissionLog(String targetEmpNo, String actorEmpNo, List<Permission> beforePerms, List<String> afterPermCodes);

    // 권한 로그 개수
    int getPermissionLogListCount();

    // 권한 로그 목록
    List<Map<String, Object>> getPermissionLogList(PageInfo pi);

    // 전자 결재 로그 개수
    int getApprovalLogListCount();

    // 전자 결재 목록
    List<Map<String, Object>> getApprovalLogList(PageInfo pi);

}
