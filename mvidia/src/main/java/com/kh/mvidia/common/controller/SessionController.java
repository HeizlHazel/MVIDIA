package com.kh.mvidia.common.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SessionController {
	
	@PostMapping("/session/extend")
	@ResponseBody
	public String extendSession(HttpSession session) {
		// 현재 세션의 마지막 접근 시간을 갱신하여 세션 만료 시간을 연장
		session.setAttribute("justToRenewSession", System.currentTimeMillis()); // 이처럼 더미 속성을 추가하면 세션이 갱신됩니다.
		return "success";
	}
}
