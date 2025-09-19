package com.kh.mvidia.common.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
	
	@ModelAttribute("sessionSeconds")
	public Long sessionSeconds(HttpSession session) {
		// 세션이 만료되지 않았다면 남은 시간을 계산하여 반환
		if (session != null && session.getLastAccessedTime() != 0) {
			long lastAccessedTime = session.getLastAccessedTime();
			long maxInactiveInterval = session.getMaxInactiveInterval() * 1000L;
			long now = System.currentTimeMillis();
			long remainingSeconds = (lastAccessedTime + maxInactiveInterval - now) / 1000;
			return Math.max(0, remainingSeconds); // 음수 방지
		}
		return 0L;
	}
}
