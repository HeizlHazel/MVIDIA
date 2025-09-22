package com.kh.mvidia.common.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SessionController {
	
	@PostMapping("/session/extend")
	@ResponseBody
	public ResponseEntity<Void> extendSession(HttpSession session){
		session.setAttribute("justToRenewSession", System.currentTimeMillis());
		return ResponseEntity.noContent().build(); // 204
	}
}
