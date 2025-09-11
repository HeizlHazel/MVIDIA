package com.kh.mvidia.product.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.product.model.vo.ProductQuality;

import java.util.ArrayList;

public interface ProductService {

    // 생산제품 리스트 페이징 서비스
    int selectListCount();
    ArrayList<ProductQuality> selectList(PageInfo pi);
}
