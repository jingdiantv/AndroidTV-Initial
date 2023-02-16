package com.zeewain.search.data.source.http.service;


import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.search.data.protocol.request.ProductModuleListReq;
import com.zeewain.search.data.protocol.request.SearchReq;
import com.zeewain.search.data.protocol.response.ProductListMo;
import com.zeewain.search.data.protocol.response.SearchResp;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SearchService {
    @POST(BaseConstants.basePath +"/search/product/query")
    Observable<BaseResp<SearchResp>> getSearchInfo(@Body SearchReq searchReq);

    @POST(BaseConstants.basePath + "/product/online/module")
    Observable<BaseResp<ProductListMo>> getProductModuleList(@Body ProductModuleListReq productModuleListReq);
}
