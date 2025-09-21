package com.kh.mvidia.common.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PageInfo {

    private int listCount;
    private int currentPage;
    private int pageLimit;
    private int boardLimit;

    private int maxPage;
    private int startPage;
    private int endPage;
}
