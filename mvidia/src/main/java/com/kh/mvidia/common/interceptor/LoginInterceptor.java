package com.kh.mvidia.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpSession session = request.getSession();
		
		// 로그인 되어 있으면 Controller 실행 허용
		if (session.getAttribute("loginEmp") != null) {
			return true;
		} else {
			// 로그인 안 돼 있으면 차단하고 로그인 페이지로 리다이렉트
			session.setAttribute("alertMsg", "로그인 후 이용가능한 서비스입니다.");
			response.sendRedirect(request.getContextPath()); // 로그인 페이지 URL로 수정
			return false;
		}
	}
}
