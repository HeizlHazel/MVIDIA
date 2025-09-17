package com.kh.mvidia.approvalNotion.controller;

import com.kh.mvidia.approvalNotion.model.dto.ApprovalDetail;
import com.kh.mvidia.approvalNotion.model.dto.ApprovalItem;
import com.kh.mvidia.approvalNotion.model.dto.NotionPageResult;
import com.kh.mvidia.approvalNotion.model.service.ApprovalServiceImpl;
import kong.unirest.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kong.unirest.HttpResponse;

import java.util.List;
import java.util.Map;

@Controller
public class approvalController {

    @Autowired
    private ApprovalServiceImpl aService;


    /* 전자결재 신청 */
    @GetMapping("approvalform")
    public String approvalForm() {
        return "approval/approvalNotionForm";
    }

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
    public String selectNotion(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            NotionPageResult pageData = aService.getDatabaseWithPaging(cursor, size);

            model.addAttribute("approvals", pageData.getResults());
            model.addAttribute("nextCursor", pageData.getNextCursor());
            model.addAttribute("hasMore", pageData.isHasMore());
            model.addAttribute("currentCursor", cursor);

            return "approval/approvalNotionList";

        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace();

            return "approval/approvalNotionList";
        }
    }

    // ✅ Ajax 전용 (JSON 반환)
    @GetMapping("approvalbox/filter")
    @ResponseBody
    public List<ApprovalItem> getFilteredApprovals(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "") String cursor) {

        return aService.getApprovalList(filter, cursor);
    }

    @GetMapping("approvalbox/data")
    @ResponseBody
    public List<ApprovalItem> getApprovalData(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String filter) {

        NotionPageResult pageData = aService.getDatabaseWithPaging(cursor, size);

        if ("all".equals(filter)) {
            return pageData.getResults();
        }

        return pageData.getResults().stream()
                .filter(item ->
                        ("pending".equals(filter) && "대기".equals(item.getStatus())) ||
                                ("approved".equals(filter) && "승인".equals(item.getStatus())) ||
                                ("rejected".equals(filter) && "반려".equals(item.getStatus()))
                )
                .toList();
    }


    @ResponseBody
    @GetMapping("/approval/detail/{pageId}")
    public ApprovalDetail getApprovalDetail(@PathVariable String pageId) {
        return aService.getApprovalDetail(pageId);
    }

    @ResponseBody
    @PostMapping("/approval/approve/{pageId}")
    public String approveApproval(@PathVariable String pageId) {
        // 노션 API를 통해 상태를 "승인"으로 업데이트하는 서비스 메서드 호출
        aService.updateApprovalStatus(pageId, "승인");
        return "success";
    }

    @ResponseBody
    @PostMapping("/approval/reject/{pageId}")
    public String rejectApproval(@PathVariable String pageId) {
        // 노션 API를 통해 상태를 "반려"로 업데이트하는 서비스 메서드 호출
        aService.updateApprovalStatus(pageId, "반려");
        return "success";
    }

}
