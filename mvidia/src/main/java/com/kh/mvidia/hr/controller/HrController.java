package com.kh.mvidia.hr.controller;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	public String updateEmp(Model model, String empNo){
		Employee emp = empService.selectEmpNo(empNo);
		Attachment atch = empService.selectProfile(empNo);
		model.addAttribute("emp", emp);
		model.addAttribute("atch", atch);
		return "/hr/updateEmpForm";
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
