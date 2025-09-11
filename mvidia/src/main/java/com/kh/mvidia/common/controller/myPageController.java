package com.kh.mvidia.common.controller;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class myPageController {
	
	@Autowired
	EmployeeService empService;
	
	@GetMapping({"/myPage", "/myPage/profile"})
	public String myPage(HttpSession session, Model model){
		Attachment atch = empService.selectProfile(((Employee)session.getAttribute("loginEmp")).getEmpNo());
		
		if(atch == null){
			atch = new Attachment();
		}
		model.addAttribute("atch", atch);
		return "/common/myPage";
	}
	
	@GetMapping("/myPage/update")
	public String updateProfile(HttpSession session, Model model){
		Attachment atch = empService.selectProfile(((Employee)session.getAttribute("loginEmp")).getEmpNo());
		
		model.addAttribute("atch", atch);
		return "/common/myPageUpdateForm";
	}
	
	
	
}
