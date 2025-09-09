package com.kh.mvidia.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class myPageController {
	
	
	@GetMapping({"/myPage", "/myPage/profile"})
	public String myPage(){
		
		return "/common/myPage";
	}
	
	
	
}
