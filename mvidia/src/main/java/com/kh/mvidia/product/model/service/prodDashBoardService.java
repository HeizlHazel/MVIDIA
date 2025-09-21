package com.kh.mvidia.product.model.service;

import java.util.List;
import java.util.Map;

public interface prodDashBoardService {

    List<Map<String,Object>> getRecentDefective();

    Map<String,Object> getSummary();

    List<Map<String,Object>> getTop5Prog();
}
