package com.kh.mvidia.product.controller;


import com.kh.mvidia.product.model.service.prodDashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class prodDashBoardController {

    @Autowired
    private prodDashBoardService prodDashboardService;

    // 대시보드 메인
    @GetMapping("/prodDashBoard.bo")
    public String dashboard(Model model) {

        List<Map<String,Object>> recentDefective = prodDashboardService.getRecentDefective();
        Map<String,Object> summary = prodDashboardService.getSummary();
        List<Map<String,Object>> top5Prog = prodDashboardService.getTop5Prog();

        model.addAttribute("recentDefective", recentDefective);
        model.addAttribute("summary", summary);
        model.addAttribute("top5Prog", top5Prog);

        return "product/prodDashBoard";
    }
}
