package com.kh.mvidia.product.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.product.model.vo.DefectiveProduction;

import java.util.ArrayList;

public interface DefectiveService {

    // 불량제품 리스트 페이징 서비스
    int selectListCount();
    ArrayList<DefectiveProduction> selectList(PageInfo pi);

    // 불량제품 등록
    int insertDefective(DefectiveProduction dp);

    // 불량제품 삭제
    int deleteDefective(ArrayList<String> defNoList);

}
