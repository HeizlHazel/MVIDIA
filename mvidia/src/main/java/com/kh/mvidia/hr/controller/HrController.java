package com.kh.mvidia.hr.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.common.model.vo.Department;
import com.kh.mvidia.common.model.vo.EmpModifyReq;
import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.integratedAtt.model.service.AttendanceService;
import com.kh.mvidia.integratedAtt.model.service.VacationService;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import com.kh.mvidia.integratedAtt.model.vo.Vacation;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/hr")
public class HrController {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	@Autowired
	private EmployeeService empService;
	
	@Autowired
	private AttendanceService attService;
	
	@Autowired
	private VacationService vaService;
	
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
	public String updateEmp(Employee emp, Attachment atch, EmpModifyReq req, @RequestParam("atchFile") MultipartFile atchFile, @RequestParam Map<String, String> reqIdMap, @RequestParam String pendingRejects, HttpSession session, RedirectAttributes redirectAttributes){

		Employee manager = (Employee) session.getAttribute("loginEmp");
		String managerId = manager.getEmpNo();
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> rejectReasons = Collections.emptyMap();
		try {
			if (pendingRejects != null && !pendingRejects.isEmpty()) {
				rejectReasons = mapper.readValue(pendingRejects, new TypeReference<Map<String, String>>() {});
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		Employee patch = new Employee();
		patch.setEmpNo(emp.getEmpNo());
		
		int resultReq = 1;     // req 상태 변경들
		int resultFile = 1;    // 파일 상태 변경/삽입
		int resultEmp = 1;     // employee 수정
		
		for (Map.Entry<String, String> entry : reqIdMap.entrySet()) {
			final String fieldName = entry.getKey();
			final String reqId     = entry.getValue();
			
			Map<String, Object> params = new HashMap<>();
			params.put("reqId", reqId);
			params.put("managerId", managerId);
			
			if ("profilePic".equals(fieldName)) {
				// 요청 건에서 old/new atch id 조회
				EmpModifyReq reqRow = empService.findEmpModifyReqById(reqId);
				String oldAtchId = reqRow.getOldAtchId();
				String newAtchId = reqRow.getNewAtchId();
				
				boolean userUploadedNew =
						(atchFile != null && atchFile.getOriginalFilename() != null && !atchFile.getOriginalFilename().equals(""));
				
				if (userUploadedNew) {
					// ③ 둘 다 아니고 "직접 업로드" 선택한 경우
					// - 새 파일 저장 & INSERT(file_status='U')
					// - 이 변경요청(req)은 거절(D), 사유는 입력값(없으면 기본)
					// - 기존 old/new 첨부는 모두 'L'
					String changeName = saveFile(atchFile); // 기존 유틸
					atch.setRefType("F");
					atch.setOriginName(atchFile.getOriginalFilename());
					atch.setChangeName(changeName);
					atch.setFileStatus("U");
					atch.setUploadEmpNo(emp.getEmpNo());
					resultFile = empService.insertFile(atch); // atchId 생성 (selectKey)
					
					params.put("reqStatus", "D");
					params.put("rejectReason", rejectReasons.getOrDefault("profilePic", "사용자 직접 업로드로 대체"));
					resultReq = empService.updateEmpModifyReqStatus(params); // 승인자/일 포함(approve_date=SYSDATE)
					
					if (oldAtchId != null){
						Attachment oldAtch = new Attachment();
						oldAtch.setAtchId(oldAtchId);
						oldAtch.setFileStatus("L");
						resultFile = empService.updateFile(oldAtch);
					}
					if (newAtchId != null){
						Attachment newAtch = new Attachment();
						newAtch.setAtchId(newAtchId);
						newAtch.setFileStatus("L");
						resultFile = empService.updateFile(newAtch);
					}
					
					// 필요 시: 방금 삽입된 atch를 프로필 반영
					// empService.applyProfileAttachment(emp.getEmpNo(), atch.getAtchId());
					
				} else if (rejectReasons.containsKey("profilePic")) {
					// ① 기존의 걸(OLD)로 쓴다 = "거절"
					// - req: D + reject_reason
					// - new_atch_id → 'L'
					params.put("reqStatus", "D");
					params.put("rejectReason", rejectReasons.get("profilePic"));
					resultReq = empService.updateEmpModifyReqStatus(params);
					
					if (newAtchId != null){
						Attachment newAtch = new Attachment();
						newAtch.setAtchId(newAtchId);
						newAtch.setFileStatus("L");
						resultFile = empService.updateFile(newAtch);
					}
					
				} else {
					// ② 변경된 값(NEW)으로 쓴다 = "승인"
					// - req: A
					// - old_atch_id → 'L', new_atch_id → 'U'
					params.put("reqStatus", "A");
					params.put("rejectReason", null);
					resultReq = empService.updateEmpModifyReqStatus(params);
					
					if (oldAtchId != null){
						Attachment oldAtch = new Attachment();
						oldAtch.setAtchId(oldAtchId);
						oldAtch.setFileStatus("L");
						resultFile = empService.updateFile(oldAtch);
					}
					if (newAtchId != null){
						Attachment newAtch = new Attachment();
						newAtch.setAtchId(newAtchId);
						newAtch.setFileStatus("U");
						resultFile = empService.updateFile(newAtch);
					}
		
				}
				
			} else {
				// 일반 필드 처리
				if (rejectReasons.containsKey(fieldName)) {
					params.put("reqStatus", "D");
					params.put("rejectReason", rejectReasons.get(fieldName));
					resultReq = empService.updateEmpModifyReqStatus(params);
				} else {
					params.put("reqStatus", "A");
					params.put("rejectReason", null);
					resultReq = empService.updateEmpModifyReqStatus(params);
					
					applyApprovedFieldToPatch(patch, emp, fieldName);
				}

			}
		}
		resultEmp = empService.updateEmpSelective(patch);
		if(resultReq * resultFile * resultEmp > 0 ){
			redirectAttributes.addFlashAttribute("alertMsg", emp.getEmpLName() + emp.getEmpName() + "사원의 정보를 변경 성공했습니다.");
			return "redirect:/hr/empAccount.hr";
		}else{
			redirectAttributes.addFlashAttribute("alertMsg", emp.getEmpLName() + emp.getEmpName() + "사원의 정보 변경에 실패했습니다.");
			return "redirect:/hr/empAccount.hr";
		}
	}
	
	@ResponseBody
	@PostMapping("/accountDelete.hr")
	public Map<String, Object> accountDeleteForm(String empNo){
		int result = empService.deleteEmp(empNo);
		
		Map<String, Object> response = new HashMap<>();
		if(result > 0) {
			response.put("success", true);
			response.put("message","사원 계정이 성공적으로 삭제되었습니다.");
		}else{
			response.put("success", false);
			response.put("message","사원 계정 삭제에 실패했습니다.");
		}
		
		return response;
	}
	
	@GetMapping("/integrated.hr")
	public String integratedPage(){
		return "/hr/integratedPage";
	}
	
	@ResponseBody
	@GetMapping("/departments")
	public ArrayList<Department> selectDeptList(){
		return empService.selectDeptList();
	}
	
	@ResponseBody
	@GetMapping("/integrated/all")
	public ArrayList<Employee> selectEmpAllList(){
		return empService.selectEmpAllList();
	}
	
	@ResponseBody
	@GetMapping("/integrated/dept/{deptName}")
	public ArrayList<Employee> selectEmpByDept(@PathVariable("deptName") String deptName){
		return empService.selectEmpByDept(deptName);
	}
	
	@GetMapping("/accountEmpDetail.hr")
	public String EmployeeDetail(@RequestParam("empNo") String empNo, Model model){
		Employee emp = empService.selectEmpNo(empNo);
		model.addAttribute("emp", emp);
		return "hr/accountEmpDetail.hr";
	}
	
	@GetMapping("/integratedAttendance.hr")
	public String integratedAttendancePage(){
		return "hr/integratedAttendancePage";
	}
	
	@ResponseBody
	@GetMapping("/vacations/recent5")
	public ArrayList<Vacation> getRecentVacations(){
		return vaService.selectRecentVacations();
	}
	
	@ResponseBody
	@GetMapping("/attendances/recent5")
	public ArrayList<Attendance> getRecentAttendance(){
		return attService.selectRecentAttendances();
	}
	
	@GetMapping("/vacationList.hr")
	public String vacationListPage(Model model,
								   @RequestParam(value="cpage", defaultValue = "1") int currentPage,
								   @RequestParam(value = "keyword", required = false) String keyword,
								   @RequestParam(value = "status", required = false) String status,
								   @RequestParam(value = "type", required = false) String type){
		
		HashMap<String, String> searchMap = new HashMap<>();
		searchMap.put("keyword", keyword);
		searchMap.put("status", status);
		searchMap.put("type", type);

		
		int listCount = vaService.selectVaListCount(searchMap);
		int pageLimit = 10;
		int boardLimit = 15;
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		
		ArrayList<Vacation> vaList = vaService.selectVacationList(pi, searchMap);
		model.addAttribute("vaList", vaList);
		model.addAttribute("pi", pi);
		return "/hr/vacationListPage";
	}
	
	@PostMapping("/vacations/update")
	@ResponseBody
	public Map<String, Object> updateVacationStatus(@RequestParam String vaId, @RequestParam String vaStatus, @RequestParam String vaCategory, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		Vacation va = new Vacation();
		va.setVaId(vaId);
		va.setVaStatus(vaStatus);
		va.setVaCategory(vaCategory);
		
		Employee authEmp =  (Employee)session.getAttribute("loginEmp");
		va.setAuthNo(authEmp.getEmpNo());
		
		int result = vaService.updateVacation(va);
		
		if (result > 0) {
			response.put("success", true);
			response.put("message", "휴가 정보가 업데이트되었습니다.");
		} else {
			response.put("success", false);
			response.put("message", "업데이트에 실패했습니다.");
		}
		return response;
	}
	
	@GetMapping("/attendanceList.hr")
	public String attendanceListPage(Model model,
									 @RequestParam(value = "cpage", defaultValue = "1") int currentPage,
									 @RequestParam(value = "keyword", required = false) String keyword,
									 @RequestParam(value = "status", required = false) String status){
		
		HashMap<String, String> searchMap = new HashMap<>();
		searchMap.put("keyword", keyword);
		searchMap.put("status", status);
		
		int listCount = attService.selectAttListCount(searchMap);
		int pageLimit = 10;
		int boardLimit = 15;
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		
		
		ArrayList<Attendance> attList = attService.selectAttendanceList(pi, searchMap);
		
		model.addAttribute("attList", attList);
		model.addAttribute("pi", pi);
		return "/hr/attendanceListPage";
	}
	
	@PostMapping("/attendances/update")
	@ResponseBody
	public Map<String, Object> updateAttendanceStatus(@RequestParam String attNo,
													  @RequestParam String attStatus,
													  @RequestParam String arrivingTime,
													  @RequestParam String leavingTime,
													  HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		Attendance att = new Attendance();
		att.setAttNo(attNo);
		att.setAttStatus(attStatus);
		att.setArrivingTime(arrivingTime);
		att.setLeavingTime(leavingTime);
		
		Employee hrmEmp = (Employee)session.getAttribute("loginEmp");
		att.setHrmNo(hrmEmp.getEmpNo());
		int result = attService.updateAttendance(att);
		
		if (result > 0) {
			response.put("success", true);
			response.put("message", "근태 정보가 업데이트되었습니다.");
		} else {
			response.put("success", false);
			response.put("message", "업데이트에 실패했습니다.");
		}
		return response;
	}
	
	@GetMapping("/certificate")
	public String certificatePage(){
		return "/certificate/certificatePage";
	}
	
	
	@ResponseBody
	@GetMapping("/checkEmpNo.hr")
	public Employee checkEmpNo(@RequestParam String empNo){
		return empService.checkEmpNo(empNo);
	}
	
	public String saveFile(MultipartFile atchFile){
		String originName =atchFile.getOriginalFilename();
		
		String currentTime =new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		int ranNum = (int)(Math.random() * 90000 + 10000);
		String ext =originName.substring(originName.lastIndexOf("."));
		
		String changeName = currentTime + ranNum +ext;
		
		File savePath =new File(uploadDir);
		if(!savePath.exists()){
			savePath.mkdirs();
		}
		
		try{
			atchFile.transferTo(new File(savePath, changeName));
		} catch (IllegalStateException | IOException e ){
			e.printStackTrace();
		}
		
		return changeName;
	}
	
	/** 승인된 fieldName에 따라 patch에 값 복사 */
	private void applyApprovedFieldToPatch(Employee patch, Employee src, String fieldName) {
		switch (fieldName) {
			case "email":         patch.setEmail(src.getEmail()); break;
			case "empLName":      patch.setEmpLName(src.getEmpLName()); break;
			case "empName":       patch.setEmpName(src.getEmpName()); break;
			case "empEngLName":   patch.setEmpEngLName(src.getEmpEngLName()); break;
			case "empEngName":    patch.setEmpEngName(src.getEmpEngName()); break;
			case "birthday":      patch.setBirthday(src.getBirthday()); break;
			case "deptCode":      patch.setDeptCode(src.getDeptCode()); break;
			case "deptName":      patch.setDeptName(src.getDeptName()); break;
			case "jobCode":       patch.setJobCode(src.getJobCode()); break;
			case "jobName":       patch.setJobName(src.getJobName()); break;
			case "address":       patch.setAddress(src.getAddress()); break;
			case "phone":         patch.setPhone(src.getPhone()); break;
			case "extNo":         patch.setExtNo(src.getExtNo()); break;
			default:
				break;
		}
	}
	
}
