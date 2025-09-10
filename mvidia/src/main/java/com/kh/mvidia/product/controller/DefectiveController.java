package com.kh.mvidia.product.controller;

import com.kh.mvidia.common.model.vo.DefPageInfo;
import com.kh.mvidia.common.template.DefPagination;
import com.kh.mvidia.product.model.service.DefectiveServiceImpl;
import com.kh.mvidia.product.model.vo.DefectiveProduction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @PostMapping("insert.bo")
    public String insertDefective( DefectiveProduction dp, Model model, RedirectAttributes redirectAttributes){

        int result = dService.insertDefective(dp);

        if(result > 0){
            redirectAttributes.addFlashAttribute("alertMsg", "성공적으로 불량 제품이 등록되었습니다.");
            return "redirect:dlist.bo";
        }else{
            model.addAttribute("errorMsg", "불량 제품 등록이 실패됐습니다.");
            return "common/errorPage";
        }
    }







}
