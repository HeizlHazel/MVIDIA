package com.kh.mvidia.certificate.controller;

import com.kh.mvidia.certificate.service.NotionCertificateService;
import com.kh.mvidia.employee.model.vo.Employee;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {
	
	@Autowired private NotionCertificateService certService;
	
	@Value("${notion.certificate.hire.id}")   private String hireDbId;    // 재직
	@Value("${notion.certificate.career.id}") private String careerDbId;  // 경력
	
	// 증명서 발급 (노션에 페이지 생성)
	@PostMapping("/issue")
	public ResponseEntity<?> issue(@RequestBody Map<String, String> body, HttpSession session){
		String type = body.get("type");
		String empNo = body.get("empNo");
		Employee emp = (Employee)session.getAttribute("loginEmp");
		String approveEmpNo = emp.getEmpNo();
		if(type==null || empNo==null) return ResponseEntity.badRequest().body(Map.of("message","type/empNo 누락"));
		
		String dbId;
		switch (type){
			case "employment": dbId = hireDbId; break;
			case "career":     dbId = careerDbId; break;
			default: return ResponseEntity.badRequest().body(Map.of("message","유효하지 않은 증명서 타입"));
		}
		
		try{
			Map<String,Object> res = certService.createCertificatePage(dbId, empNo, approveEmpNo);
			return ResponseEntity.ok(Map.of(
					"message","증명서가 성공적으로 발급되어 노션에 저장되었습니다.",
					"issueNo", res.get("issueNo"),
					"certNo",  res.get("certNo"),
					"pageId",  res.get("pageId")
			));
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of(
					"message","증명서 발급 실패: Notion 오류: " + e.getMessage()
			));
		}
	}
	
}
