package com.kh.mvidia.approvalNotion.controller;

import com.kh.mvidia.approvalNotion.model.service.ApprovalServiceImpl;
import kong.unirest.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.Map;

@Controller
public class approvalController {

    @Autowired
    private ApprovalServiceImpl aService;
]

    /* 전자결재 신청 */@GetMapping("approvalform")
    public String approvalForm(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "approval/approvalNotionForm";
    }

    /* 전자결재 문서함 */
    @GetMapping("approvalbox")
    public String approvalBox(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "approval/approvalNotionBox";
    }

    @ResponseBody
    @PostMapping(value="add.notion", consumes="application/json")
    public String addNotion(@RequestBody Map<String, String> map) {

        String applyWriter = map.get("applyWriter");
        String applyDept = map.get("applyDept");
        String applyDate = map.get("applyDate");
        String applyTitle = map.get("applyTitle");
        String approval = map.get("approval");
        String details = map.get("details");

        HttpResponse<JsonNode> response = aService.addPage(applyWriter, applyDept, applyDate, applyTitle, approval, details);

       int status = response.getStatus();
        if(status == 200) {
            return "success";
        }else {
            return "fail";
        }
    }

    @GetMapping("list.notion")
    public String selectNotion(Model model) {
        String dbData = aService.getDatabase();
        model.addAttribute("dbData", dbData);
        return "notion/notionListView";
    }

}
