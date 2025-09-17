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

        List<ScheduleRegistration> schrList = cService.selectAllSchr();
        List<ScheduleRegistration> schrTop5 = cService.selectTop5Schr();
        List<ProgressChart> progTop5 = cService.selectTop5Prog();

        model.addAttribute("schrList", schrList);
        model.addAttribute("schrTop5", schrTop5);
        model.addAttribute("progTop5", progTop5);

        return "product/chartListView";
    }

    // 불량률 + 진행률 데이터 조회 json으로 반환(ajax용)
    // -> 브라우저에서 json 가져올 때 호출함
    @GetMapping("/cplist.bo")
    @ResponseBody
    public Map<String, Object> chartBoardAjax(){

        Map<String, Object> result = new HashMap<>();
        result.put("progTop5", cService.selectTop5Prog());
        result.put("schrTop5", cService.selectTop5Schr());
        result.put("schrList", cService.selectAllSchr());
        return result;
    }

    // 전체 일정 페이지
    @GetMapping("/schrList.bo")
    public String scheduleList(Model model){
        List<ScheduleRegistration> schrList = cService.selectAllSchr();
        model.addAttribute("schrList", schrList);
        return "product/scheduleAllListView";
    }


}
