package com.kh.mvidia.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class adminController {

    /* 관리자 대시보드 */
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/adminDashboard";
    }
    /* 계정 권한 관리 */
    @GetMapping("/permission")
    public String grantRevoke(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/grantRevokePermission";
    }

    /* 사용자 계정 잠금 해제 */
    @GetMapping("/unlock")
    public String unlockAccount(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/unlockAccount";
    }

    /* 부서 생성/수정/삭제 */
    @GetMapping("/dept")
    public String deptManage(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/departmentManage";
    }

    /* ERP UI 설정 */
    @GetMapping("/ui")
    public String uiSettings(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "admin/uiSettings";
    }

}
