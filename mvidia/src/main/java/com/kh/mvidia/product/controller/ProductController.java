package com.kh.mvidia.product.controller;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.product.model.service.ProductServiceImpl;
import com.kh.mvidia.product.model.vo.ProductQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductServiceImpl pService;

    // 생산 제품 조회 페이지 (검색 + 페이징 + ajax)
    @GetMapping("plist.bo")
    public String prodlist(@RequestParam(value = "cpage", defaultValue = "1") int currentPage,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "ajax", required = false, defaultValue = "false") boolean ajax,
                                 Model model) {

        if(keyword == null) keyword = "";

        int listCount = pService.selectListCount(keyword);

        PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 10, 5);
        List<ProductQuality> list = pService.selectList(pi, keyword);

        model.addAttribute("pi", pi);
        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);

        if(ajax){ return "fragments/productTable :: prodTable"; }


        return "product/productListView";
    }


    // 불량 등록용 제품 조회 (AJAX에서 호출)
    @GetMapping("allprod.bo")
    @ResponseBody
    public List<ProductQuality> getAllProduct(){
        return pService.selectAllList();
    }
}
