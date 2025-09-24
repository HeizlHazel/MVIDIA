package com.kh.mvidia.attManage.controller;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.integratedAtt.model.service.AttendanceService;
import com.kh.mvidia.integratedAtt.model.service.VacationService;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import com.kh.mvidia.integratedAtt.model.vo.Vacation;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/attManage")
public class AttManageController {
	
	@Autowired
	private AttendanceService attService;
	
	@Autowired
	private VacationService vaService;
	
	@GetMapping("/attendanceRecordCheck.attManage")
	public String attendanceRecordCheckPage(Model model,
											@RequestParam(value = "cpage", defaultValue = "1") int currentPage,
											@RequestParam(value = "keyword", required = false) String keyword,
											@RequestParam(value = "status", required = false) String status,
											HttpSession session){
		
		HashMap<String, String> searchMap = new HashMap<>();
		searchMap.put("keyword", keyword);
		searchMap.put("status", status);
		
		Employee emp =  (Employee) session.getAttribute("loginEmp");
		searchMap.put("empNo", emp.getEmpNo());
		
		int listCount = attService.selectEmpAttListCount(searchMap);
		int pageLimit = 10;
		int boardLimit = 15;
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		
		
		ArrayList<Attendance> attList = attService.selectEmpAttendanceList(pi, searchMap);
		
		model.addAttribute("attList", attList);
		model.addAttribute("pi", pi);
		return "/attManage/attendanceRecordCheckPage";
	}
	
	@GetMapping("/requestVacation.attManage")
	public String requestVacation(){
		return "/attManage/requestVacationPage";
	}
	
	@GetMapping("/userVacation.attManage")
	public String useVacation(Model model,
							  @RequestParam(value = "cpage", defaultValue = "1") int currentPage,
							  @RequestParam(value = "keyword", required = false) String keyword,
							  @RequestParam(value = "status", required = false) String status,
							  @RequestParam(value = "type", required = false) String type,
							  HttpSession session){
		
		HashMap<String, String> searchMap = new HashMap<>();
		searchMap.put("keyword", keyword);
		searchMap.put("status", status);
		searchMap.put("type", type);
		
		Employee emp =  (Employee) session.getAttribute("loginEmp");
		searchMap.put("empNo", emp.getEmpNo());
		
		int listCount = vaService.selectEmpVaListCount(searchMap);
		int pageLimit = 10;
		int boardLimit = 15;
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		
		ArrayList<Vacation> vaList = vaService.selectEmpVacationList(pi, searchMap);
		
		model.addAttribute("vaList", vaList);
		model.addAttribute("pi", pi);
		
		return "/attManage/userVacationPage";
	}
	
	@GetMapping("/inquiryCalendar.attManage")
	public String inquirtCalendar(){
		return "/calendar/inquiryCalendarPage";
	}
	
	@PostMapping("/insertVacation.attManage")
	public String insertVacation(Vacation va, RedirectAttributes redirectAttributes){
		int result = vaService.insertVacation(va);
		
		if(result > 0 ){
			redirectAttributes.addFlashAttribute("alertMsg", "휴가 신청에 성공했습니다. 휴가 사용내역 페이지에서 신청 승인 상황을 확인가능합니다.");
			return "redirect:/attManage/userVacation.attManage";
		}else{
			redirectAttributes.addFlashAttribute("alertMsg", "휴가 신청에 실패했습니다.");
			return "redirect:/mainPage";
		}
		
	}
}
