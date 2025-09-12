package com.kh.mvidia.hr.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.common.model.vo.EmpModifyReq;
import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/hr")
public class HrController {
	
	@Autowired
	private EmployeeService empService;
	
	@GetMapping("/hrMainPage")
	public String hrMainPage(){
		return "hr/hrMainPage";
	}
	
	@GetMapping("/empAccount.hr")
	public String empAccountPage(){
		return "/hr/empAccountPage";
	}
	
	@GetMapping("/accountCreate.hr")
	public String accountCreateForm(){
		return "/hr/accountCreateFormPage";
	}
	
	@GetMapping("/accountModifyDetail.hr")
	public String accountModifyDetail(Model model, String empNo){
		Employee emp = empService.selectEmpNo(empNo);
		Attachment atch = empService.selectProfile(empNo);
		ArrayList<EmpModifyReq> reqList = empService.selectReq(empNo);
		model.addAttribute("emp", emp);
		model.addAttribute("atch", atch);
		model.addAttribute("reqList", reqList);
		return "/hr/updateEmpForm";
	}
	
	@PostMapping("/updateEmp.hr")
	public String updateEmp(Employee emp, Attachment atch, EmpModifyReq req, RedirectAttributes redirectAttributes){
		// 1. 세션에서 로그인한 관리자 정보 가져오기
		Employee manager = (Employee) session.getAttribute("loginEmp");
		String managerId = manager.getEmpNo();
		
		// 2. 거절 사유가 담긴 JSON 문자열을 Map으로 파싱
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> rejectReasons = null;
		try {
			rejectReasons = mapper.readValue(pendingRejects, new TypeReference<Map<String, String>>() {});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		// 3. 변경 요청 상태 업데이트
		for (String fieldName : reqIdMap.keySet()) {
			String reqId = reqIdMap.get(fieldName);
			
			// Attachment와 관련된 특별 처리
			if (fieldName.equals("profilePic")) {
				// ... (아래에서 상세 로직 설명)
				// 첨부파일 관련 로직이 복잡하여 별도로 분리하는 것이 좋습니다.
			} else {
				// 기타 필드 (이름, 주소 등)
				Map<String, Object> params = new HashMap<>();
				params.put("reqId", reqId);
				params.put("managerId", managerId);
				
				if (rejectReasons.containsKey(fieldName)) {
					// 거절된 경우: reqStatus를 'D'로
					params.put("reqStatus", "D");
					params.put("rejectReason", rejectReasons.get(fieldName));
				} else {
					// 승인된 경우: reqStatus를 'A'로
					params.put("reqStatus", "A");
					params.put("rejectReason", null);
				}
				empModifyReqService.updateEmpModifyReqStatus(params);
			}
		}
		
		return "redirect:/hr/empAccount.hr";
	}
	
	@GetMapping("/accountDelete.hr")
	public String accountDeleteForm(){
		return "/hr/accountDeleteFormPage";
	}
	
	@ResponseBody
	@GetMapping("/checkEmpNo.hr")
	public Employee checkEmpNo(@RequestParam String empNo){
		return empService.checkEmpNo(empNo);
	}
	
}
