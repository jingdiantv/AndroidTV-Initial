package com.zeewain.search.data.protocol.request;

import com.zeewain.search.conf.Constant;

public class SearchReq {
    private String categoryId = "";
    private int pageSize;
    private String searchKey;
    private int pageNo;

    public SearchReq(String searchKey, int pageNo) {
        this.searchKey = searchKey;
        this.pageNo = pageNo;
        pageSize = Constant.SEARCH_RESULT_PAGE_SIZE;
    }
}
