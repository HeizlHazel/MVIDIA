package com.kh.mvidia.product.controller;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.product.model.service.DefectiveServiceImpl;
import com.kh.mvidia.product.model.vo.DefectiveProduction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

        PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 10, 5);
        ArrayList<DefectiveProduction> list = dService.selectList(pi);

        mv.addObject("pi", pi).addObject("list", list).setViewName("product/defectiveListview");

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

    // 등록된 불량 제품 삭제 기능 (체크박스로 다중 선택 가능)
    /*
    @PostMapping("delete.bo")
    @ResponseBody
    public String deleteDefective(@RequestParam("dno") ArrayList<String> defNoList){

        int result = dService.deleteDefective(defNoList);

        if(result > 0){ // 성공

            redirectAttributes.addFlashAttribute("alertMsg", "선택한 항목이 삭제되었습니다.");
            return "redirect:dlist.bo";
        }else{ // 실패

            model.addAttribute("errorMsg", "선택한 항목을 삭제할 수 없습니다. 잠시 후 다시 시도해주세요.");
            return "common/errorPage";
        }

    }

     */







}
