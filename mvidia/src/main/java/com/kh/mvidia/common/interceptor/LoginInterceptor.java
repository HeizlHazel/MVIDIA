package com.kh.mvidia.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpSession session = request.getSession();
		
		String requestURI = request.getRequestURI();
		
		// 1. 이미 로그인된 상태라면, 정상적으로 요청 처리
		if (session.getAttribute("loginEmp") != null) {
			return true;
		}
		
		// 2. 로그인 페이지 또는 관련 리소스는 인터셉터 검사 대상에서 제외
		// /로 바로 들어오거나, /login.emp와 같은 로그인 처리 URL일 경우
		if (requestURI.equals(request.getContextPath() + "/") || requestURI.equals(request.getContextPath() + "/login.emp")) {
			return true;
		}
		
		// 3. 위 조건에 해당하지 않으면서 로그인되지 않았다면, 로그인 페이지로 리다이렉트
		session.setAttribute("redirectUrl", requestURI);
		session.setAttribute("alertMsg", "로그인 후 이용가능한 서비스입니다.");
		response.sendRedirect(request.getContextPath());
		return false;
	}
		
	
}
