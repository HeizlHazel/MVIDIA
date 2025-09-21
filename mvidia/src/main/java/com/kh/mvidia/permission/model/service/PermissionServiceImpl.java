package com.kh.mvidia.permission.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.permission.model.dao.PermissionDao;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.EmpPermission;
import com.kh.mvidia.permission.model.vo.Permission;
import com.kh.mvidia.permission.model.vo.SystemLog;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    // 권한 업데이트
    @Override
    @Transactional
    public void updatePermission(String empNo, List<String> newPermCodes) {
        try {
            System.out.println("=== 권한 업데이트 시작 ===");
            System.out.println("사용자: " + empNo);
            System.out.println("새로운 권한: " + newPermCodes);

            // 1. 변경 전 권한 상태 조회
            List<Permission> beforePerms = pDao.selectPermList(sqlSession, empNo);

            // 2. 기존 권한을 Map으로 구성 (권한코드 -> Permission 객체)
            Map<String, Permission> existingPermsMap = new HashMap<>();
            Set<String> beforePermCodes = new HashSet<>();

            for (Permission perm : beforePerms) {
                existingPermsMap.put(perm.getPermCode(), perm);
                if ("Y".equals(perm.getIsGranted())) {
                    beforePermCodes.add(perm.getPermCode());
                }
            }

            // 3. 변경 후 권한 코드
            Set<String> afterPermCodes = new HashSet<>(newPermCodes != null ? newPermCodes : new ArrayList<>());

            // 4. 추가된 권한들 처리 (N->Y 또는 없던것->Y)
            Set<String> addedPerms = new HashSet<>(afterPermCodes);
            addedPerms.removeAll(beforePermCodes);

            for (String permCode : addedPerms) {
                EmpPermission ep = new EmpPermission();
                ep.setUserId(empNo);
                ep.setPermCode(permCode);
                ep.setIsGranted("Y");

                // row 존재 여부 직접 조회
                int exists = pDao.empPermissionRowExists(sqlSession, empNo, permCode);

                if (exists > 0) {
                    // row 존재 -> update
                    System.out.println("권한 활성화: " + permCode + " (N -> Y) - update 실행");
                    int result = pDao.updateEmpPermission(sqlSession, ep);
                    if (result == 0) {
                        System.err.println("⚠️ updateEmpPermission 실패 - 대상 row 없음: " + empNo + ", " + permCode);
                    }
                    System.out.println("업데이트 결과: " + result);
                } else {
                    // row 없음 -> insert
                    System.out.println("권한 신규 부여: " + permCode + " - insert 실행");
                    int result = pDao.insertEmpPermission(sqlSession, ep);
                    if (result == 0) {
                        System.err.println("⚠️ insert 실패 - " + empNo + " / " + permCode);
                    }
                    System.out.println("삽입 결과: " + result);
                }
            }

            // 5. 제거된 권한들 처리 (Y->N)
            Set<String> removedPerms = new HashSet<>(beforePermCodes);
            removedPerms.removeAll(afterPermCodes);

            for (String permCode : removedPerms) {
                EmpPermission ep = new EmpPermission();
                ep.setUserId(empNo);
                ep.setPermCode(permCode);
                ep.setIsGranted("N");

                System.out.println("권한 비활성화: " + permCode + " (Y -> N)");
                int result = pDao.deleteEmpPermission(sqlSession, ep);
                System.out.println("비활성화 결과: " + result);
            }

            System.out.println("=== 권한 업데이트 완료 ===");

        } catch (Exception e) {
            System.err.println("권한 업데이트 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 권한 가져와서 세션에 저장
    @Override
    public List<Permission> getUserGrantedPermissions(String empNo) {
        List<Permission> allPerms = pDao.selectPermList(sqlSession, empNo);
        List<Permission> grantedPerms = new ArrayList<>();

        for (Permission perm : allPerms) {
            if ("Y".equals(perm.getIsGranted())) {
                grantedPerms.add(perm);
            }
        }

        return grantedPerms;
    }

    // 권한 변경 이력 DB 저장 - 실제 변경사항만 로그
    @Override
    public void savePermissionLog(String targetEmpNo, String actorEmpNo, List<Permission> beforePerms, List<String> afterPermCodes) {
        try {
            Employee targetEmp = selectEmployee(targetEmpNo);
            Employee actorEmp = selectEmployee(actorEmpNo);

            String empName = targetEmp.getEmpLName() + targetEmp.getEmpName();
            String actorName = actorEmp.getEmpLName() + actorEmp.getEmpName();

            // 변경 전 권한 상태 분석
            Set<String> beforePermCodes = new HashSet<>();  // Y인 권한들
            Map<String, String> permCodeToNameMap = new HashMap<>();

            if (beforePerms != null) {
                for (Permission perm : beforePerms) {
                    permCodeToNameMap.put(perm.getPermCode(), perm.getPermName());
                    if ("Y".equals(perm.getIsGranted())) {
                        beforePermCodes.add(perm.getPermCode());  // 변경 전 Y인 것만
                    }
                }
            }

            // 변경 후 권한 상태
            Set<String> afterPermCodeSet = new HashSet<>(afterPermCodes != null ? afterPermCodes : new ArrayList<>());

            // 실제 변경사항 분석
            // 1. 새로 추가된 권한: N -> Y (또는 없던 것 -> Y)
            Set<String> grantedPerms = new HashSet<>(afterPermCodeSet);
            grantedPerms.removeAll(beforePermCodes);  // 기존에 없던 것들만 남음

            // 2. 제거된 권한: Y -> N
            Set<String> revokedPerms = new HashSet<>(beforePermCodes);
            revokedPerms.removeAll(afterPermCodeSet);  // 새로운 목록에 없는 것들만 남음

            System.out.println("=== 권한 변경 분석 ===");
            System.out.println("변경 전 권한: " + beforePermCodes);
            System.out.println("변경 후 권한: " + afterPermCodeSet);
            System.out.println("새로 부여된 권한: " + grantedPerms);
            System.out.println("회수된 권한: " + revokedPerms);

            int logCount = 0;

            // 부여된 권한 로그 (N->Y 또는 없던것->Y)
            for (String permCode : grantedPerms) {
                String permName = permCodeToNameMap.get(permCode);
                if (permName != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("logType", "PERMISSION");
                    params.put("targetId", targetEmpNo);
                    params.put("targetName", permName);
                    params.put("actorId", actorEmpNo);
                    params.put("action", "GRANT");
                    params.put("reason", "권한 부여");

                    pDao.insertPermissionLog(sqlSession, params);
                    System.out.println("✅ 권한 부여 로그: " + empName + " - " + permName);
                    logCount++;
                }
            }

            // 회수된 권한 로그 (Y->N)
            for (String permCode : revokedPerms) {
                String permName = permCodeToNameMap.get(permCode);
                if (permName != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("logType", "PERMISSION");
                    params.put("targetId", targetEmpNo);
                    params.put("targetName", permName);
                    params.put("actorId", actorEmpNo);
                    params.put("action", "REVOKE");
                    params.put("reason", "권한 회수");

                    pDao.insertPermissionLog(sqlSession, params);
                    System.out.println("❌ 권한 회수 로그: " + empName + " - " + permName);
                    logCount++;
                }
            }

            // 변경사항 요약
            if (logCount == 0) {
                System.out.println("⚪ 실제 권한 변경사항 없음: " + empName + " (Y->Y, N->N만 있음)");
            } else {
                System.out.println("📝 총 " + logCount + "개의 권한 변경 로그 저장 완료");
            }

        } catch (Exception e) {
            System.err.println("권한 로그 저장 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public int getPermissionLogListCount() {
        try {
            return pDao.selectPermissionLogListCount(sqlSession);
        } catch (Exception e) {
            System.err.println("권한 로그 카운트 조회 실패: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> getPermissionLogList(PageInfo pi) {
        try {
            Map<String, Object> params = new HashMap<>();

            // Oracle의 경우
            params.put("startRow", (pi.getCurrentPage() - 1) * pi.getBoardLimit() + 1);
            params.put("endRow", pi.getCurrentPage() * pi.getBoardLimit());

            // MySQL/PostgreSQL의 경우
            // params.put("offset", (pi.getCurrentPage() - 1) * pi.getBoardLimit());
            // params.put("boardLimit", pi.getBoardLimit());

            List<Map<String, Object>> logList = pDao.selectPermissionLogList(sqlSession, params);

            System.out.println("권한 로그 조회 결과: " + logList.size() + "건");
            return logList;

        } catch (Exception e) {
            System.err.println("권한 로그 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public int getApprovalLogListCount() {
        try {
            return pDao.selectApprovalLogListCount(sqlSession);
        } catch (Exception e) {

            System.err.println("전자결재 로그 카운트 조회 실패: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> getApprovalLogList(PageInfo pi) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startRow", (pi.getCurrentPage() - 1) * pi.getBoardLimit() + 1);
            params.put("endRow", pi.getCurrentPage() * pi.getBoardLimit());

            List<Map<String, Object>> logList = pDao.selectApprovalLogList(sqlSession, params);

            System.out.println("전자결재 로그 조회 결과: " + logList.size() + "건");
            return logList;

        } catch (Exception e) {
            System.err.println("전자결재 로그 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
