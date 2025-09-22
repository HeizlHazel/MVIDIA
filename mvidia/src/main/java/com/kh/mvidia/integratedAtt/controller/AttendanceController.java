package com.kh.mvidia.integratedAtt.controller;

import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.integratedAtt.model.service.AttendanceService;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import jakarta.servlet.http.HttpSession;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RequestMapping("/att")
public class AttendanceController {
	
	@Autowired
	private AttendanceService attService;
	
	@ResponseBody
	@PostMapping("/checkin")
	public HashMap<String, Object> checkin(HttpSession session){
		Employee emp = (Employee) session.getAttribute("loginEmp");
		return attService.checkInUpsert(emp.getEmpNo());
	
	}
	
	@ResponseBody
	@PostMapping("/checkout")
	public HashMap<String, Object> checkout(HttpSession session){
		Employee emp = (Employee) session.getAttribute("loginEmp");
		return attService.checkOut(emp.getEmpNo());
	}
	
	@ResponseBody
	@GetMapping("/today")
	public HashMap<String, Object> attToday(HttpSession session){
		Employee emp = (Employee) session.getAttribute("loginEmp");
		HashMap<String,Object> res = new HashMap<>();
		
		Attendance t = attService.selectTodayTimes(emp.getEmpNo());
		res.put("arriving", t != null ? t.getArrivingTime() : null);
		res.put("leaving",  t != null ? t.getLeavingTime()  : null);
		res.put("status",   t != null ? t.getAttStatus()    : null);
		res.put("ok", true);
		return res;
	}
	
}
