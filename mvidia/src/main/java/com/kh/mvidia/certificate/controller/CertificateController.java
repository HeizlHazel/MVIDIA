package com.kh.mvidia.certificate.controller;

import com.kh.mvidia.certificate.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {
	
	@Autowired
	private CertificateService certService;
	
	// application.properties에서 각 DB ID를 주입받습니다.
	@Value("${notion.certificate.hire.id}")
	private String hireDatabaseId;
	@Value("${notion.certificate.career.id}")
	private String careerDatabaseId;
	@Value("${notion.certificate.pay.id}")
	private String payDatabaseId;
	
	// DB에서 사원 정보를 조회하는 엔드포인트입니다.
	@GetMapping("/employee")
	public ResponseEntity<Map<String, Object>> getEmployeeInfo(@RequestParam String empNo) {
		if ("12345678".equals(empNo)) {
			Map<String, Object> employeeData = new HashMap<>();
			employeeData.put("empNo", empNo);
			employeeData.put("empName", "홍길동");
			employeeData.put("jobName", "대리");
			employeeData.put("deptName", "경영지원팀");
			employeeData.put("rrn", "000000-0000000");
			employeeData.put("address", "서울특별시 어딘가");
			employeeData.put("hireDate", "2020.03.15");
			employeeData.put("phone", "010-1234-5678");
			
			employeeData.put("companyName", "주식회사 엠비디아");
			employeeData.put("companyAddress", "서울특별시 강남구 테헤란로 123");
			employeeData.put("ceoName", "김대표");
			employeeData.put("bizRegNo", "123-45-67890");
			employeeData.put("companyPhone", "02-123-4567");
			
			List<Map<String, String>> careerRows = Arrays.asList(
					createCareerRow("2019.01.01", "2020.03.14", "기술팀", "서비스 개발"),
					createCareerRow("2020.03.15", null, "경영지원팀", "총무/인사")
			);
			employeeData.put("careerRows", careerRows);
			
			return ResponseEntity.ok(employeeData);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	private Map<String, String> createCareerRow(String start, String end, String department, String role) {
		Map<String, String> row = new HashMap<>();
		row.put("start", start);
		if (end != null) {
			row.put("end", end);
			row.put("current", "false");
		} else {
			row.put("current", "true");
		}
		row.put("department", department);
		row.put("role", role);
		return row;
	}
	
	// 증명서 발급 및 노션 DB에 저장하는 엔드포인트입니다.
	@PostMapping("/issue")
	public ResponseEntity<Map<String, String>> issueCertificate(@RequestBody Map<String, String> payload) {
		String type = payload.get("type");
		String empNo = payload.get("empNo");
		
		String databaseId;
		String title;
		switch (type) {
			case "employment":
				databaseId = hireDatabaseId;
				title = "재직증명서";
				break;
			case "career":
				databaseId = careerDatabaseId;
				title = "경력증명서";
				break;
			default:
				return ResponseEntity.badRequest().body(Collections.singletonMap("message", "유효하지 않은 증명서 타입입니다."));
		}
		
		try {
			certService.createCertificatePage(databaseId, title, empNo);
			return ResponseEntity.ok(Collections.singletonMap("message", "증명서가 성공적으로 발급되었습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Collections.singletonMap("message", "증명서 발급에 실패했습니다."));
		}
	}
	
	// 급여 명세서를 노션에서 조회하는 엔드포인트입니다.
	@GetMapping("/payslip")
	public ResponseEntity<Map<String, Object>> getPayslip(@RequestParam String empNo) {
		try {
			Map<String, Object> payslipData = certService.getPayslipData(payDatabaseId, empNo);
			if (payslipData != null) {
				return ResponseEntity.ok(payslipData);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).build();
		}
	}
}
