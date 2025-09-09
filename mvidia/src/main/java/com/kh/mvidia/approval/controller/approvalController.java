package com.kh.mvidia.approval.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class approvalController {

    @GetMapping("approval")
    public String approvalBox(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "approval/approvalBox";
    }

}
