package com.kh.mvidia.orgainizationalChart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class organizationalChartController {
	
	@GetMapping("/organizationalChart")
	public String organizationalChartPage() {
		return ("organizationalChart/organizationalChartPage");
	}
}
