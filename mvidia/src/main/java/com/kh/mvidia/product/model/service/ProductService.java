package com.kh.mvidia.product.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.product.model.vo.ProductQuality;

import java.util.List;

public interface ProductService {

    // 생산제품 리스트 페이징 서비스
    int selectListCount(String keyword);
    List<ProductQuality> selectList(PageInfo pi, String keyword);

    // 불량제품 등록 시 사용할 조회용
    List<ProductQuality> selectAllList();
}
