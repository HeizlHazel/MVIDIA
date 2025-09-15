package com.kh.mvidia.product.controller;

import com.kh.mvidia.product.model.service.ChartService;
import com.kh.mvidia.product.model.vo.ProgressChart;
import com.kh.mvidia.product.model.vo.ScheduleRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChartController {

    @Autowired
    private ChartService cService;

    // 화면뷰 페이지용 메소드)
    // chart.js를 동적으로 받아오기 위해 그래프 페이지 따로 두기
    @GetMapping("/clist.bo")
    public String chartboard(Model model){

        List<ProgressChart> progTop5 = cService.selectTop5Prog();
        List<ScheduleRegistration> schrTop5 = cService.selectTop5Schr();

        model.addAttribute("progTop5", progTop5);
        model.addAttribute("schrTop5", schrTop5);

        return "product/chartListView";
    }

    // 불량률 + 진행률 데이터 조회 json으로 반환(ajax용)
    // -> 브라우저에서 json 가져올 때 호출함
    @GetMapping("/cplist.bo")
    @ResponseBody
    public Map<String, Object> ChartData(){

        Map<String, Object> result = new HashMap<>();
        return result;
    }

    // 대시보드 페이지 이동
    @GetMapping("/dashboard.bo")
    public String chartDashboard(Model model){
        // 종료임박순 5개 일정만 조회
        List<ScheduleRegistration> schrList = cService.selectTop5Schr();
        model.addAttribute("dashboardData", schrList);

        // 진행률 데이터 (샘플/정적)
        List<ProgressChart> progList = cService.selectProgList();
        model.addAttribute("progressData", progList);

        return "chart/chartListView";
    }

    // 전체 일정 페이지
    @GetMapping("/schrAllList.bo")
    public String scheduleList(Model model){
        List<ScheduleRegistration> fullList = cService.selectAllSchr();
        model.addAttribute("fullScheduleList", fullList);
        return "chart/fullScheduleListView";
    }



}
