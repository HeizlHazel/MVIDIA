package com.kh.mvidia.certificate.controller;

import com.kh.mvidia.certificate.service.NotionCertificateService;
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
	@Value("${notion.certificate.pay.id}")    private String payDbId;     // 급여
	
	// 증명서 발급 (노션에 페이지 생성)
	@PostMapping("/issue")
	public ResponseEntity<?> issue(@RequestBody Map<String, String> body){
		String type = body.get("type");
		String empNo = body.get("empNo");
		if(type==null || empNo==null) return ResponseEntity.badRequest().body(Map.of("message","type/empNo 누락"));
		
		String dbId;
		switch (type){
			case "employment": dbId = hireDbId; break;
			case "career":     dbId = careerDbId; break;
			default: return ResponseEntity.badRequest().body(Map.of("message","유효하지 않은 증명서 타입"));
		}
		
		try{
			Map<String,Object> res = certService.createCertificatePage(dbId, empNo, null);
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
	
	// 급여 명세서 조회
	@GetMapping("/payslip")
	public ResponseEntity<?> payslip(@RequestParam String empNo){
		try{
			Map<String,Object> map = certService.getPayslipData(payDbId, empNo);
			if(map==null) return ResponseEntity.notFound().build();
			return ResponseEntity.ok(map);
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of(
					"message","급여 명세서 조회 실패: Notion 오류: " + e.getMessage()
			));
		}
	}
}
