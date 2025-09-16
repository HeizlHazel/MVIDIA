package com.kh.mvidia.approvalNotion.controller;

import com.kh.mvidia.approvalNotion.model.service.ApprovalServiceImpl;
import kong.unirest.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kong.unirest.HttpResponse;
import java.util.Map;

@Controller
public class approvalController {

    @Autowired
    private ApprovalServiceImpl aService;


    /* 전자결재 신청 */
    @GetMapping("approvalform")
    public String approvalForm() {
//    public String approvalForm(Model model) {
//        model.addAttribute("sessionSeconds", 3600);
        return "approval/approvalNotionForm";
    }

    /* 전자결재 문서함
    @GetMapping("approvalbox")
    public String approvalBox(Model model) {
        model.addAttribute("sessionSeconds", 3600);
        return "approval/approvalNotionBox";
    }
    */

    @ResponseBody
    @PostMapping(value="add.notion", consumes="application/json")
    public String addNotion(@RequestBody Map<String, String> map) {

        System.out.println("받은 데이터: " + map);

        String writer = map.get("applyWriter");
        String dept = map.get("applyDept");
        String date = map.get("applyDate");
        String title = map.get("applyTitle");
        String approval = map.get("approval");
        String details = map.get("details");
        String category = map.get("swtch");

        System.out.println("노션 API 호출 전"); // 호출 전 로그
        HttpResponse<JsonNode> response = aService.addPage(writer, dept, date, title, approval, details, category);
        System.out.println("노션 API 응답: " + response.getStatus() + ", " + response.getBody()); // 응답 로그

        int status = response.getStatus();



        if(status == 200) {
            return "success";
        }else {
            return "fail";
        }
    }

    @GetMapping("approvalbox")
    public String selectNotion(Model model) {
        String dbData = aService.getDatabase();
        model.addAttribute("dbData", dbData);
        return "approval/approvalNotionList";
    }

}
