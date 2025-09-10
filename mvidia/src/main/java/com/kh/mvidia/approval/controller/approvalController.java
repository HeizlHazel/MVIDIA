package com.kh.mvidia.approval.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class approvalController {

    /* 전자결재 신청 */
    @GetMapping("approvalform")
    public String approvalForm(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "approval/approvalForm";
    }

    /* 전자결재 문서함 */
    @GetMapping("approvalbox")
    public String approvalBox(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "approval/approvalBox";
    }

}
