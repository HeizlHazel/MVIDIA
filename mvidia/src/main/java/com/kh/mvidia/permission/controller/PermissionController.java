package com.kh.mvidia.permission.controller;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.permission.model.service.PermissionService;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.Permission;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class PermissionController {

    @Autowired
    private PermissionService pService;

    /* 계정 권한 관리 */
    @GetMapping("permission")
    public String grantRevoke() {
        return "admin/grantRevokePermission";
    }

    /* 사용자 검색 기능 */
    @GetMapping("search1.pe")
    @ResponseBody
    public Employee searchEmployee(@RequestParam String empNo) {
        return pService.selectEmployee(empNo);
    }

    /* 사용자 기존 권한 조회 */
    @PostMapping("searchEmpPerm.pe")
    @ResponseBody
    public List<Permission> searchEmpPermission(@RequestParam String empNo) {
        return pService.selectPermissionList(empNo);
    }

    /* 권한 부여/회수 기능 */
    @PostMapping("updatePerm.pe")
    @ResponseBody
    public Map<String, Object> updatePermission(String empNo,
                                                @RequestParam(value="permCodes", required=false) List<String> list,
                                                HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (list == null) {
                list = new ArrayList<>();
            }

            // 변경 전 권한 조회 (로그용)
            List<Permission> beforePerms = pService.selectPermissionList(empNo);

            // 권한 업데이트 (첫 번째 트랜잭션)
            pService.updatePermission(empNo, list);

            // 로그 저장 (별도 처리 - 실패해도 권한 변경은 유지)
            try {
                Employee loginEmp = (Employee) session.getAttribute("loginEmp");
                if (loginEmp != null) {
                    System.out.println("로그 저장 시작 - loginEmp: " + loginEmp.getEmpNo());
                    pService.savePermissionLog(empNo, loginEmp.getEmpNo(), beforePerms, list);
                } else {
                    System.out.println("ERROR: 세션에 loginEmp가 없습니다!");
                }
            } catch (Exception logError) {
                System.err.println("로그 저장 실패 (권한 변경은 성공): " + logError.getMessage());
                logError.printStackTrace();
            }

            result.put("success", true);
            result.put("message", "권한이 성공적으로 저장되었습니다.");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "권한 저장 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public boolean hasPermission(HttpSession session, String permCode) {
        Set<String> grantedPerms = (Set<String>) session.getAttribute("grantedPerms");
        return grantedPerms != null && grantedPerms.contains(permCode);
    }

    /* 권한 변경 이력 */
    @GetMapping("permlog")
    public String permissionLog(@RequestParam(defaultValue = "1") int currentPage, Model model) {

        try {
            int listCount = pService.getPermissionLogListCount();
            PageInfo pageInfo = Pagination.getPageInfo(listCount, currentPage, 10, 10);
            List<Map<String, Object>> logList = pService.getPermissionLogList(pageInfo);

            // JavaScript용으로 키 이름을 소문자로 변환
            List<Map<String, Object>> jsLogList = new ArrayList<>();
            for (Map<String, Object> log : logList) {
                Map<String, Object> jsLog = new HashMap<>();
                jsLog.put("action", log.get("ACTION"));
                jsLog.put("target_name", log.get("TARGET_NAME"));
                jsLog.put("target_emp_name", log.get("TARGET_EMP_NAME"));
                jsLog.put("dept_name", log.get("DEPT_NAME"));
                jsLog.put("actor_name", log.get("ACTOR_NAME"));
                jsLog.put("created_at", log.get("CREATED_AT"));
                jsLogList.add(jsLog);
            }

            // JavaScript에서 사용할 데이터를 HTML에 직접 삽입
            model.addAttribute("jsLogList", jsLogList);
            model.addAttribute("jsPageInfo", pageInfo);
            model.addAttribute("jsTotalCount", listCount);
            model.addAttribute("jsCurrentPage", currentPage);

        } catch (Exception e) {
            System.err.println("권한 로그 조회 중 오류: " + e.getMessage());
            model.addAttribute("jsLogList", new ArrayList<>());
            model.addAttribute("jsTotalCount", 0);
        }

        return "admin/permissionLog";
    }

    /* 전자결재 승인/반려 이력 */
    @GetMapping("/approvelog")
    public String approvalLog(@RequestParam(defaultValue = "1") int currentPage, Model model) {

        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrentPage(currentPage);
        pageInfo.setBoardLimit(10);

        List<Map<String, Object>> logList = pService.getApprovalLogList(pageInfo);

        System.out.println("=== 컨트롤러에서 받은 데이터 ===");
        for (Map<String, Object> log : logList) {
            System.out.println("데이터: " + log);
        }

        System.out.println("Model에 전달할 데이터 크기: " + logList.size());
        System.out.println("Model에 전달할 실제 데이터: " + logList);

        model.addAttribute("jsLogList", logList);
        model.addAttribute("jsTotalCount", logList.size());
        model.addAttribute("jsCurrentPage", currentPage);

        return "admin/approvalLog";
    }

}
