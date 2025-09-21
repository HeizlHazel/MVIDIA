package com.kh.mvidia.product.controller;

import com.kh.mvidia.product.model.service.ChartService;
import com.kh.mvidia.product.model.vo.ProgressChart;
import com.kh.mvidia.product.model.vo.ScheduleRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

        List<ScheduleRegistration> schrTop5 = cService.selectTop5Schr();
        List<ScheduleRegistration> schrTop5Donut = cService.selectTop5SchrDonut();
        List<ProgressChart> progTop5 = cService.selectTop5Prog();

        List<ScheduleRegistration> schrList = cService.selectAllSchr(null);
        List<ScheduleRegistration> schrDonutList = cService.selectAllSchrDonut(null);

        model.addAttribute("schrTop5", schrTop5);
        model.addAttribute("schrTop5Donut", schrTop5Donut);
        model.addAttribute("progTop5", progTop5);

        model.addAttribute("schrList", schrList);
        model.addAttribute("schrDonutList", schrDonutList);

        return "product/chartListView";
    }

    // 불량률 + 진행률 데이터 조회 json으로 반환(ajax용)
    // -> 브라우저에서 json 가져올 때 호출함
    @GetMapping("/cplist.bo")
    @ResponseBody
    public Map<String, Object> chartBoardAjax(){

        Map<String, Object> result = new HashMap<>();

        result.put("schrTop5", cService.selectTop5Schr());
        result.put("schrTop5Donut", cService.selectTop5SchrDonut());
        result.put("progTop5", cService.selectTop5Prog());

        result.put("schrList", cService.selectAllSchr(null));
        result.put("schrDonutList", cService.selectAllSchrDonut(null));

        return result;
    }

    // 전체 일정 페이지
    @GetMapping("/schrList.bo")
    public String scheduleList(@RequestParam(value = "bpPartner", required = false) String bpPartner, Model model){

        List<ScheduleRegistration> schrList = cService.selectAllSchr(bpPartner);
        List<ScheduleRegistration> schrDonutList = cService.selectAllSchrDonut(bpPartner);

        model.addAttribute("schrList", schrList);
        model.addAttribute("schrDonutList", schrDonutList);

        return "product/scheduleAllListView";
    }

    @GetMapping("/schrListAjax.bo")
    @ResponseBody
    public Map<String, Object> scheduleListAjax(@RequestParam("bpPartner") String bpPartner){
        Map<String, Object> result = new HashMap<>();

        // 특정 업체의 데이터만 조회
        result.put("schrList", cService.selectAllSchr(bpPartner));
        result.put("schrDonutList", cService.selectAllSchrDonut(bpPartner));

        return result;
    }



}
