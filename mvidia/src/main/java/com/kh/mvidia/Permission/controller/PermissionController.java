package com.kh.mvidia.Permission.controller;

import com.kh.mvidia.Permission.model.service.PermissionService;
import com.kh.mvidia.Permission.model.service.PermissionServiceImpl;
import com.kh.mvidia.Permission.model.vo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PermissionController {

    @Autowired
    private PermissionServiceImpl pService;

    /* 관리자 대시보드 */
    @GetMapping("admin")
    public String adminDashboard(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/adminDashboard";
    }

    /* 계정 권한 관리 */
    @GetMapping("permission")
    public String grantRevoke(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/grantRevokePermission";
    }

    @GetMapping("search1.do")
    @ResponseBody
    public Employee searchEmp(@RequestParam String empNo) {
        return pService.selectEmp(empNo);
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
