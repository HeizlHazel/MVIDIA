package com.kh.mvidia.product.model.service;

import com.kh.mvidia.common.model.vo.DefPageInfo;
import com.kh.mvidia.product.model.vo.DefectiveProduction;

import java.util.ArrayList;

public interface DefectiveService {

    // 게시판 리스트 페이징 서비스
    int selectListCount();
    ArrayList<DefectiveProduction> selectList(DefPageInfo dpi);


}
