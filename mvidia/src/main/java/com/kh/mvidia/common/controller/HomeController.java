package com.kh.mvidia.common.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
	
	@GetMapping("/")
	public String index(HttpSession session, RedirectAttributes redirectAttributes){
		if(session.getAttribute("loginEmp") != null) {
			return "redirect:/mainPage";
		}else {
			// 인터셉터가 세션에 저장한 메시지를 가져옵니다.
			String alertMsg = (String) session.getAttribute("alertMsg");
			
			if (alertMsg != null) {
				// 메시지를 Flash Attribute로 옮깁니다.
				redirectAttributes.addFlashAttribute("alertMsg", alertMsg);
				// 다음 요청을 위해 세션에서 메시지를 제거합니다.
				session.removeAttribute("alertMsg");
				session.removeAttribute("alertMsg");
			}
			return "common/loginPage";
		}
	}
	
	@GetMapping("/mainPage")
	public String main(Model model) {
		model.addAttribute("sessionSeconds", 3600);
		return "common/mainPage";
	}

	
}
