package com.kh.mvidia.attManage.controller;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.integratedAtt.model.service.AttendanceService;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/attManage")
public class AttManageController {
	
	@Autowired
	private AttendanceService attService;
	
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
		
		int listCount = attService.selectAttListCount(searchMap);
		int pageLimit = 10;
		int boardLimit = 15;
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		
		
		ArrayList<Attendance> attList = attService.selectEmpAttendanceList(pi, searchMap);
		
		model.addAttribute("attList", attList);
		model.addAttribute("pi", pi);
		return "/attManage/attendanceRecordCheckPage";
	}
}
