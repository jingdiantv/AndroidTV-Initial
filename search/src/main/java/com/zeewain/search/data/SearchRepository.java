package com.zeewain.search.data;

import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zeewain.search.data.protocol.request.ProductModuleListReq;
import com.zeewain.search.data.protocol.request.SearchReq;
import com.zeewain.search.data.protocol.response.ProductListMo;
import com.zeewain.search.data.protocol.response.SearchResp;
import com.zeewain.search.data.source.http.service.SearchService;

import io.reactivex.Observable;
import retrofit2.http.Body;

public class SearchRepository {
    private static volatile SearchRepository instance;
    private final SearchService userService;

    private SearchRepository(SearchService userService){
        this.userService = userService;
    }

    public static SearchRepository getInstance(){
        if(instance == null){
            synchronized (SearchRepository.class) {
                if (instance == null){
                    instance = new SearchRepository(RetrofitClient.getInstance().create(SearchService.class));
                }
            }
        }
        return instance;
    }

    public  Observable<BaseResp<SearchResp>> getSearchInfo(@Body SearchReq searchReq){
        return userService.getSearchInfo(searchReq);
    }

    public Observable<BaseResp<ProductListMo>> getProductModuleList(ProductModuleListReq productModuleListReq){
        return userService.getProductModuleList(productModuleListReq);
    }
}
