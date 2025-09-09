package com.kh.mvidia.product.controller;

import com.kh.mvidia.common.model.vo.DefPageInfo;
import com.kh.mvidia.common.template.DefPagination;
import com.kh.mvidia.product.model.service.DefectiveServiceImpl;
import com.kh.mvidia.product.model.vo.DefectiveProduction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

@Controller
public class DefectiveController {

    @Autowired
    private DefectiveServiceImpl dService;

    // 불량 등록 조회 페이지 -> 조회 메소드
    @GetMapping("dlist.bo")
    public ModelAndView deflist(@RequestParam(value = "cpage", defaultValue = "1") int currentPage, ModelAndView mv) {

        int listCount = dService.selectListCount();

        DefPageInfo dpi = DefPagination.getDefPageInfo(listCount, currentPage, 10, 5);
        ArrayList<DefectiveProduction> list = dService.selectList(dpi);

        mv.addObject("dpi", dpi).addObject("list", list).setViewName("product/defectiveListview");

        return mv;
    }

    // 불량 등록 조회 페이지 -> 등록 메소드
    @GetMapping("benrollForm.bo")
    public String enrollForm() {

        return "product/defEnrollForm";
    }







}
