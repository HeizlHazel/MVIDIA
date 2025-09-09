package com.kh.mvidia.common.template;

import com.kh.mvidia.common.model.vo.DefPageInfo;

public class DefPagination {

    public static DefPageInfo getDefPageInfo(int listCount, int currentPage, int pageLimit, int boardLimit){

        int maxPage = (int)Math.ceil((double)listCount/boardLimit);
        int startPage = (currentPage -1) / pageLimit * pageLimit + 1;
        int endPage = startPage + pageLimit -1 ;
        if(endPage > maxPage){
            endPage = maxPage;
        }
        return new DefPageInfo(listCount, currentPage, pageLimit, boardLimit, maxPage, startPage, endPage);
    }
}
