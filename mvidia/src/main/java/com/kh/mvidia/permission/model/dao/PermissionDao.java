package com.kh.mvidia.permission.model.dao;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.permission.model.vo.EmpPermission;
import com.kh.mvidia.permission.model.vo.Permission;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.SystemLog;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PermissionDao {

    // 사용자 검색
    public Employee selectEmployee(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectOne("adminMapper.selectEmployee", empNo);
    }

    // 전체 권한 목록 + 사용자 기존 권한 조회
    public List<Permission> selectPermList(SqlSessionTemplate sqlSession, String empNo) {
        return sqlSession.selectList("adminMapper.selectPermissionList", empNo);
    }

    // 권한 row 존재 여부 조회
    public int empPermissionRowExists(SqlSessionTemplate sqlSession, String userId, String permCode) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("permCode", permCode);
        return sqlSession.selectOne("adminMapper.empPermissionRowExists", paramMap);
    }

    // 권한 부여
    public int insertEmpPermission(SqlSessionTemplate sqlSession, EmpPermission ep) {
        return sqlSession.insert("adminMapper.insertEmpPermission", ep);
    }

    // 권한 변경(N -> Y)
    public int updateEmpPermission(SqlSessionTemplate sqlSession, EmpPermission ep) {
        return sqlSession.update("adminMapper.updateEmpPermission", ep);
    }

    // 권한 제거
    public int deleteEmpPermission(SqlSessionTemplate sqlSession, EmpPermission ep) {
        return sqlSession.update("adminMapper.deleteEmpPermission", ep);
    }

    // 전자결재 승인 이력 로그 저장
    public int insertApprovalLog(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        return sqlSession.insert("adminMapper.insertApprovalLog", params);
    }

    // 권한 변경 이력 로그 저장
    public int insertPermissionLog(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        return sqlSession.insert("adminMapper.insertPermissionLog", params);
    }

    // 권한 로그 총 개수
    public int selectPermissionLogListCount(SqlSessionTemplate sqlSession) {
        return sqlSession.selectOne("adminMapper.selectPermissionLogListCount");
    }

    // 권한 로그 목록
    public List<Map<String, Object>> selectPermissionLogList(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        return sqlSession.selectList("adminMapper.selectPermissionLogList", params);
    }

    // 전자결재 로그 총 개수
    public int selectApprovalLogListCount(SqlSessionTemplate sqlSession) {
        return sqlSession.selectOne("adminMapper.selectApprovalLogListCount");
    }

    // 전자결재 로그 목록
    public List<Map<String, Object>> selectApprovalLogList(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        return sqlSession.selectList("adminMapper.selectApprovalLogList", params);
    }

    // 결재권자 부장 직급 조회
    public List<Employee> selectManagerEmployees(SqlSessionTemplate sqlSession){
        return sqlSession.selectList("adminMapper.selectManagerEmployees");
    }
}
