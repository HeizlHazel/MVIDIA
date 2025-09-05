package com.kh.mvidia.employee.controller;

import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
public class EmployeeController {
	
	private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService empService;
	
	@Autowired
	private PasswordEncoder bcryptPasswordEncoder;

    @PostMapping("login.emp")
    public String loginEmp(Employee emp, HttpSession session, RedirectAttributes redirectAttributes){
		
		logger.info("User Input - empNo: {}, empPwd: {}", emp.getEmpNo(), emp.getEmpPwd());

		
		Employee loginEmp =  empService.loginEmp(emp);
		System.out.println("되나" + loginEmp);
		
		logger.info("DB Data - empNo: {}, empPwd: {}", loginEmp.getEmpNo(), loginEmp.getEmpPwd());
		boolean passwordMatches = bcryptPasswordEncoder.matches(emp.getEmpPwd(), loginEmp.getEmpPwd());
		logger.info("Password matches: {}", passwordMatches);
		
		if(loginEmp != null && bcryptPasswordEncoder.matches(emp.getEmpPwd(), loginEmp.getEmpPwd())) {
			

			
			session.setAttribute("loginEmp", loginEmp);
			return "redirect:/mainPage";
		}else{
			redirectAttributes.addFlashAttribute("alertMsg", "로그인에 실패하였습니다. 다시 로그인 시도해주세요.");
			return "redirect:/";
		}
    }
	
	@PostMapping("logout.emp")
	public String logoutEmp(HttpSession session){
		session.invalidate();
		return "redirect:/";
	}
	
	
}

