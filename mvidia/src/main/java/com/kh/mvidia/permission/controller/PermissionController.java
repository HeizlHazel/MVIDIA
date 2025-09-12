package com.kh.mvidia.permission.controller;

import com.kh.mvidia.permission.model.service.PermissionService;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.vo.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PermissionController {

    @Autowired
    private PermissionService pService;

    /* 계정 권한 관리 */
    @GetMapping("permission")
    public String grantRevoke(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/grantRevokePermission";
    }

    /* 권한 변경 이력 */
    @GetMapping("permlog")
    public String permissionLog(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/permissionLog";
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
    public String updatePermission(String empNo, @RequestParam(value="permCodes", required=false) List<String> list, Model model) {
        if (list == null) {
            list = new ArrayList<>();
        }
        pService.updatePermission(empNo, list);
        return "admin/grantRevokePermission";
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
