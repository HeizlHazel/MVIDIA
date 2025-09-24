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
	public String updateEmp(
			Employee emp,
			@RequestParam(value="atchFile", required=false) MultipartFile atchFile,
			@RequestParam(required=false) Map<String, String> reqIdMap,
			HttpSession session,
			RedirectAttributes redirectAttributes) {
		
		if (reqIdMap == null) reqIdMap = Collections.emptyMap();
		Employee manager = (Employee) session.getAttribute("loginEmp");
		String managerId = manager != null ? manager.getEmpNo() : null;
		
		// patch 객체 (승인된 필드만 반영)
		Employee patch = new Employee();
		patch.setEmpNo(emp.getEmpNo());
		
		int reqOps = 0;
		int fileOps = 0;
		
		// 1) 프로필 사진 처리
		boolean userUploadedNew = (atchFile != null
				&& atchFile.getOriginalFilename() != null
				&& !atchFile.getOriginalFilename().isEmpty());
		
		if (userUploadedNew) {
			// 선강등: 기존 U -> L
			fileOps += (empService.demoteAllToLByEmp(emp.getEmpNo()) >= 0 ? 1 : 0);
			
			// 새 파일 U 등록
			String changeName = saveFile(atchFile);
			Attachment up = new Attachment();
			up.setRefType("F");
			up.setOriginName(atchFile.getOriginalFilename());
			up.setChangeName(changeName);
			up.setUploadEmpNo(emp.getEmpNo());
			up.setFileStatus("U");
			fileOps += (empService.insertFile(up) > 0 ? 1 : 0);
			
			// profilePic 요청이 있으면 승인 처리
			if (reqIdMap.containsKey("profilePic")) {
				Map<String, Object> params = new HashMap<>();
				params.put("reqId", reqIdMap.get("profilePic"));
				params.put("managerId", managerId);
				params.put("reqStatus", "A");
				reqOps += (empService.updateEmpModifyReqStatus(params) > 0 ? 1 : 0);
			}
		}
		
		// 업로드는 없고, 변경요청(profilePic)만 들어온 상태라면 NEW를 U로 승격
		if (!userUploadedNew && reqIdMap.containsKey("profilePic")) {
			String reqId = reqIdMap.get("profilePic");
			EmpModifyReq picReq = empService.findEmpModifyReqById(reqId);
			String oldAtchId = picReq != null ? picReq.getOldAtchId() : null;
			String newAtchId = picReq != null ? picReq.getNewAtchId() : null;
			
			// 선강등
			fileOps += (empService.demoteAllToLByEmp(emp.getEmpNo()) >= 0 ? 1 : 0);
			
			// old L, new U
			if (oldAtchId != null) {
				Attachment a = new Attachment(); a.setAtchId(oldAtchId); a.setFileStatus("L");
				fileOps += (empService.updateFile(a) > 0 ? 1 : 0);
			}
			if (newAtchId != null) {
				Attachment a = new Attachment(); a.setAtchId(newAtchId); a.setFileStatus("U");
				fileOps += (empService.updateFile(a) > 0 ? 1 : 0);
			}
			
			// 요청 승인
			Map<String, Object> params = new HashMap<>();
			params.put("reqId", reqId);
			params.put("managerId", managerId);
			params.put("reqStatus", "A");
			reqOps += (empService.updateEmpModifyReqStatus(params) > 0 ? 1 : 0);
		}
		
		// 2) 일반 필드 요청들 승인 + patch 반영 (부서/직급은 제외 표시만 하는 요구였다면 그대로 두고, 여기선 승인/반영 로직 유지)
		for (Map.Entry<String, String> e : reqIdMap.entrySet()) {
			String fieldName = e.getKey();
			if ("profilePic".equals(fieldName)) continue;
			
			Map<String, Object> params = new HashMap<>();
			params.put("reqId", e.getValue());
			params.put("managerId", managerId);
			params.put("reqStatus", "A");
			reqOps += (empService.updateEmpModifyReqStatus(params) > 0 ? 1 : 0);
			
			applyApprovedFieldToPatch(patch, emp, fieldName);
		}
		
		// 3) patch 반영
		int empOps = empService.updateEmpSelective(patch);
		
		// 4) ✅ 세션 즉시 동기화 (로그인 사용자가 이번에 변경된 사원과 동일한 경우)
		try {
			Employee loginEmp = (Employee) session.getAttribute("loginEmp");
			if (loginEmp != null && loginEmp.getEmpNo() != null
					&& loginEmp.getEmpNo().equals(emp.getEmpNo())) {
				// 최신 활성(U) 프로필 조회
				Attachment latest = empService.selectProfile(emp.getEmpNo()); // file_status='U' 한 건
				if (latest != null && latest.getChangeName() != null) {
					// (1) 세션의 로그인 사용자 객체에 즉시 반영
					loginEmp.setChangeName(latest.getChangeName());
					session.setAttribute("loginEmp", loginEmp);
					
					// (2) 호환성을 위해 atch 세션도 함께 갱신 (다른 템플릿/코드에서 사용할 가능성)
					session.setAttribute("atch", latest);
				} else {
					// 활성 프로필이 없으면 null로 초기화(기본 이미지 쓰도록)
					loginEmp.setChangeName(null);
					session.setAttribute("loginEmp", loginEmp);
					session.removeAttribute("atch");
				}
			}
		} catch (Exception ignore) { /* 세션 갱신 실패해도 본 처리엔 영향 없음 */ }
		
		boolean ok = (reqOps >= 0) && (fileOps >= 0) && (empOps >= 0);
		redirectAttributes.addFlashAttribute("alertMsg",
				ok ? emp.getEmpLName() + emp.getEmpName() + " 사원의 정보를 변경했습니다."
						: emp.getEmpLName() + emp.getEmpName() + " 사원 정보 변경에 실패했습니다.");
		
		return "redirect:/hr/empAccount.hr";
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
	public String certificatePage(HttpSession session, Model model){
		Employee confirmer = (Employee)session.getAttribute("loginEmp");
		model.addAttribute("confirmer", confirmer);
		return "/certificate/certificatePage";
	}
	
	@GetMapping("/accountDetail.hr")
	public String accountDetailPage(@RequestParam("empNo") String empNo, Model model){
		Employee emp = empService.selectEmpNo(empNo);
		Attachment atch = empService.selectProfile(empNo);
		if (atch == null) atch = new Attachment();
		model.addAttribute("emp", emp);
		model.addAttribute("atch", atch);
		return "/organizationalChart/accountDetailPage";
	}
	
	
	@ResponseBody
	@GetMapping("/checkEmpNo.hr")
	public Employee checkEmpNo(@RequestParam String empNo){
		return empService.checkEmpNo(empNo);
	}
	
	@ResponseBody
	@GetMapping("/checkEmpNoCer.hr")
	public Employee checkEmpNoCer(@RequestParam String empNo){
		return empService.checkEmpNoCer(empNo);
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
