package com.kh.mvidia.permission.controller;

import com.kh.mvidia.permission.model.service.PermissionServiceImpl;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.EmpPermission;
import com.kh.mvidia.permission.model.vo.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class PermissionController {

    @Autowired
    private PermissionServiceImpl pService;

    /* 계정 권한 관리 */
    @GetMapping("permission")
    public String grantRevoke(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/grantRevokePermission";
    }

    /* 권한 변경 이력 */
    @GetMapping("permlog")
    public String permLog(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/permissionLog";
    }

    /* 사용자 검색 기능 */
    @GetMapping("search1.pe")
    @ResponseBody
    public Employee searchEmp(@RequestParam String empNo) {
        return pService.selectEmp(empNo);
    }

    /* 전체 권한 목록 조회 */
    @PostMapping("searchPerm.pe")
    @ResponseBody
    public List<Permission> searchPermission() {
        return pService.selectPermList();
    }

    /* 권한 부여/회수 기능 */
    @PostMapping("updatePerm.pe")
    public List<EmpPermission> updatePerm(String empNo) {
    // 사용자 기존 권한 조회
        return pService.selectEmpPermList(empNo);
}

    /* 사용자 계정 잠금 해제 */
    @GetMapping("unlock")
    public String unlockAccount(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/unlockAccount";
    }

    /* 부서 생성/수정/삭제 */
    @GetMapping("dept")
    public String deptManage(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/departmentManage";
    }

    /* ERP UI 설정 */
    @GetMapping("ui")
    public String uiSettings(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/uiSettings";
    }



}
