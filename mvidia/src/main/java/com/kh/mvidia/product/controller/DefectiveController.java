package com.kh.mvidia.product.controller;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.common.template.Pagination;
import com.kh.mvidia.product.model.service.DefectiveServiceImpl;
import com.kh.mvidia.product.model.service.ProductService;
import com.kh.mvidia.product.model.vo.DefectiveProduction;
import com.kh.mvidia.product.model.vo.ProductQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DefectiveController {

    @Autowired
    private DefectiveServiceImpl dService;

    @Autowired
    private ProductService pService;

    // 불량 등록 조회 페이지 -> 조회 메소드
    @GetMapping("dlist.bo")
    public ModelAndView deflist(@RequestParam(value = "cpage", defaultValue = "1") int currentPage,
                                @RequestParam(value = "ajax", required = false, defaultValue = "false") boolean ajax,
                                ModelAndView mv) {

        int listCount = dService.selectListCount();

        PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 10, 5);
        ArrayList<DefectiveProduction> list = dService.selectList(pi);

        mv.addObject("pi", pi).addObject("list", list);

        if(ajax){
            mv.setViewName("fragments/DefProductTable :: defTable");
        }else{
            mv.setViewName("product/defectiveListView");
        }

        return mv;
    }

    // 불량 등록 조회 페이지 -> 등록 메소드
    @GetMapping("benrollForm.bo")
    public String enrollForm(Model model) {

        List<ProductQuality> productList = pService.selectAllList();
        model.addAttribute("productList", productList);
        return "product/defEnrollForm";
    }

    // 등록 메서드를 Ajax용으로 수정
    @PostMapping("insert.bo")
    @ResponseBody
    public Map<String, Object> insertDefective(DefectiveProduction dp) {

        Map<String, Object> response = new HashMap<>();

        try {
            int result = dService.insertDefective(dp);

            if(result > 0) {
                response.put("result", "success");
                response.put("message", "성공적으로 불량 제품이 등록되었습니다.");
            } else {
                response.put("result", "failure");
                response.put("message", "불량 제품 등록이 실패됐습니다.");
            }
        } catch (Exception e) {
            response.put("result", "error");
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }

    // 등록된 불량 제품 삭제 기능 (체크박스로 다중 선택 가능)
    @PostMapping("delete.bo")
    @ResponseBody
    public Map<String, Object> deleteDefective(@RequestParam("dno") ArrayList<String> defNoList){

        Map<String, Object> response = new HashMap<>();

        try {
            int result = dService.deleteDefective(defNoList);

            if(result > 0) { // 성공
                response.put("result", "success");
                response.put("message", "선택한 항목이 삭제되었습니다.");
                response.put("deletedCount", result);
                response.put("deletedItems", defNoList);
            } else { // 실패
                response.put("result", "failure");
                response.put("message", "선택한 항목을 삭제할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (Exception e) {
            response.put("result", "error");
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
        }

        return response;

    }

    @GetMapping("search.bo")
    @ResponseBody
    public Map<String, Object> searchDefective(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "defType", required = false) String defType,
            @RequestParam(value = "defStatus", required = false) String defStatus,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "cpage", defaultValue = "1") int currentPage) {

        Map<String, Object> params = new HashMap<>();

        // null이 아니고 빈 값이 아닌 경우만 params에 추가
        if (keyword != null && !keyword.trim().isEmpty()) {
            params.put("keyword", keyword.trim());
        }
        if (defType != null && !defType.trim().isEmpty()) {
            params.put("defType", defType);
        }
        if (defStatus != null && !defStatus.trim().isEmpty()) {
            params.put("defStatus", defStatus);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            params.put("startDate", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            params.put("endDate", endDate);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // 디버깅용 로그
            System.out.println("검색 파라미터: " + params);

            int listCount = dService.selectSearchCount(params);
            System.out.println("검색 결과 수: " + listCount);

            PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 10, 5);
            ArrayList<DefectiveProduction> list = dService.selectSearchList(params, pi);

            response.put("result", "success");
            response.put("list", list);
            response.put("pi", pi);

            if (listCount == 0) {
                response.put("message", "검색 조건에 맞는 결과가 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 상세 오류 출력
            response.put("result", "error");
            response.put("message", "검색 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }

}
