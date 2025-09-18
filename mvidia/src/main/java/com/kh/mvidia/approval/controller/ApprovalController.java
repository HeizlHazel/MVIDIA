package com.kh.mvidia.approval.controller;

import com.kh.mvidia.approval.model.dto.ApprovalDetail;
import com.kh.mvidia.approval.model.dto.ApprovalItem;
import com.kh.mvidia.approval.model.service.ApprovalServiceImpl;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.service.PermissionServiceImpl;
import jakarta.servlet.http.HttpSession;
import kong.unirest.JsonNode;
import kong.unirest.HttpResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ApprovalController {

    @Autowired
    private ApprovalServiceImpl aService;

    @Autowired
    private PermissionServiceImpl pService;

    // =================== 페이지 렌더링 ===================

    // 전자결재 신청 폼
    @GetMapping("approvalform")
    public String approvalForm() {
        return "approval/approvalForm";
    }

    // 전자결재 문서함 (내가 작성한 문서)
    @GetMapping("approvalbox")
    public String approvalBox(@RequestParam(defaultValue = "all") String filter,
                              HttpSession session, Model model) {
        try {
            Employee loginEmp = (Employee) session.getAttribute("loginEmp");

            if (loginEmp != null) {
                List<ApprovalItem> myDocuments = aService.getMyDocuments(loginEmp, filter);
                model.addAttribute("approvals", myDocuments);
                model.addAttribute("totalCount", myDocuments.size());
            } else {
                model.addAttribute("approvals", Collections.emptyList());
                model.addAttribute("totalCount", 0);
            }

            model.addAttribute("filter", filter);
            return "approval/approvalBox";

        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("approvals", Collections.emptyList());
            model.addAttribute("totalCount", 0);
            model.addAttribute("filter", filter);
            return "approval/approvalBox";
        }
    }

    // 결재 승인함 (내가 결재해야 할 문서)
    @GetMapping("approvalmanage")
    public String approvalManage(HttpSession session, Model model) {
        Employee loginEmp = (Employee) session.getAttribute("loginEmp");

        if (loginEmp != null) {
            int pendingCount = aService.getPendingApprovalCount(loginEmp);
            model.addAttribute("pendingApprovalCount", pendingCount);
            System.out.println("결재 대기 문서 수: " + pendingCount);
        } else {
            model.addAttribute("pendingApprovalCount", 0);
        }

        return "approval/approvalManage";
    }

    // =================== API 엔드포인트 ===================

    // 전자결재 등록
    @ResponseBody
    @PostMapping(value = "add.notion", consumes = "application/json")
    public String addNotion(@RequestBody Map<String, String> map, HttpSession session) {
        System.out.println("받은 데이터: " + map);

        String writer = map.get("applyWriter");
        String dept = map.get("applyDept");
        String date = map.get("applyDate");
        String title = map.get("applyTitle");
        String approval = map.get("approval");
        String details = map.get("details");
        String category = map.get("swtch");

        System.out.println("문서 작성자: " + writer);

        HttpResponse<JsonNode> response = aService.addPage(writer, dept, date, title, approval, details, category);
        System.out.println("노션 API 응답: " + response.getStatus() + ", " + response.getBody());

        return response.getStatus() == 200 ? "success" : "fail";
    }

    // 문서함 데이터 조회 (AJAX용)
    @GetMapping("approvalbox/data")
    @ResponseBody
    public List<ApprovalItem> getApprovalBoxData(@RequestParam(defaultValue = "all") String filter,
                                                 HttpSession session) {
        Employee loginEmp = (Employee) session.getAttribute("loginEmp");

        if (loginEmp == null) {
            return Collections.emptyList();
        }

        return aService.getMyDocuments(loginEmp, filter);
    }

    // 승인함 데이터 조회 (AJAX용)
    @GetMapping("approval-manage/data")
    @ResponseBody
    public List<ApprovalItem> getApprovalManageData(@RequestParam(defaultValue = "pending") String tab,
                                                    HttpSession session) {
        Employee loginEmp = (Employee) session.getAttribute("loginEmp");

        if (loginEmp == null) {
            return Collections.emptyList();
        }

        return aService.getMyApprovalDocuments(loginEmp, tab);
    }

    // 문서 상세 조회
    @ResponseBody
    @GetMapping("/approval/detail/{pageId}")
    public ApprovalDetail getApprovalDetail(@PathVariable String pageId) {
        return aService.getApprovalDetail(pageId);
    }

    // 결재 승인
    @ResponseBody
    @PostMapping("/approval/approve/{pageId}")
    public String approveApproval(@PathVariable String pageId) {
        aService.updateApprovalStatus(pageId, "승인");
        return "success";
    }

    // 결재 반려
    @ResponseBody
    @PostMapping("/approval/reject/{pageId}")
    public String rejectApproval(@PathVariable String pageId) {
        aService.updateApprovalStatus(pageId, "반료");
        return "success";
    }

    // 실시간 대기 건수 조회
    @GetMapping("api/approval/pending-count")
    @ResponseBody
    public Map<String, Object> getPendingCountApi(HttpSession session) {
        Employee loginEmp = (Employee) session.getAttribute("loginEmp");
        Map<String, Object> result = new HashMap<>();

        if (loginEmp != null) {
            int pendingCount = aService.getPendingApprovalCount(loginEmp);
            result.put("count", pendingCount);
            result.put("success", true);
        } else {
            result.put("count", 0);
            result.put("success", false);
        }

        return result;
    }

    // 메인 페이지에서 사용할 대기 건수 (다른 컨트롤러에서 사용)
    @ModelAttribute("pendingApprovalCount")
    public int addPendingCountToModel(HttpSession session) {
        Employee loginEmp = (Employee) session.getAttribute("loginEmp");
        if (loginEmp != null) {
            return aService.getPendingApprovalCount(loginEmp);
        }
        return 0;
    }
}