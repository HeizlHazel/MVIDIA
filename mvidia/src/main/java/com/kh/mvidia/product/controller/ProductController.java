package com.kh.mvidia.product.controller;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.product.model.service.ProductServiceImpl;
import com.kh.mvidia.product.model.vo.ProductQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

@Controller
public class ProductController {

    @Autowired
    private ProductServiceImpl pService;

    // 생산 제품 조회 페이지
    @GetMapping("plist.bo")
    public ModelAndView prodlist(@RequestParam(value = "cpage", defaultValue = "1") int currentPage, ModelAndView mv) {

        int listCount = pService.selectListCount();

        PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 10, 5);
        ArrayList<ProductQuality> list = pService.selectList(pi);

        mv.addObject("pi", pi).addObject("list", list).setViewName("product/productListView");

        return mv;
    }
}
