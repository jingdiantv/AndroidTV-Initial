package com.zeewain.search.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.search.conf.Constant;
import com.zeewain.search.data.SearchRepository;
import com.zeewain.search.data.model.CourseInfo;
import com.zeewain.search.data.protocol.request.ProductModuleListReq;
import com.zeewain.search.data.protocol.request.SearchReq;
import com.zeewain.search.data.protocol.response.ProductListMo;
import com.zeewain.search.data.protocol.response.SearchResp;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SearchViewModel extends BaseViewModel {
    private static final String TAG = "SearchViewModel";

    private final SearchRepository searchRepository;

    public MutableLiveData<LoadState> pSearchState = new MutableLiveData<>();
    public MutableLiveData<LoadState> pHotSearchState = new MutableLiveData<>();
    public MutableLiveData<LoadState> pSearchHistory = new MutableLiveData<>();

    public List<CourseInfo> pSearchInfo = new ArrayList<>();
    public List<CourseInfo> pHotSearchResult = new ArrayList<>();
    private List<String> mSearchHistoryList;

    public SearchViewModel(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public void reqSearchResult(String searchContent, int pageNumber) {
        SearchReq searchReq = new SearchReq(searchContent, pageNumber);
        pSearchState.setValue(LoadState.Loading);
        searchRepository.getSearchInfo(searchReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<SearchResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<SearchResp> response) {
                        assert (pageNumber > 0) : "搜索结果传入的pageNumber异常 小于等于0";
                        if (pageNumber == 1) pSearchInfo.clear();
                        for (SearchResp.RecordInfo recordInfo: response.data.getRecords()) {
                            CourseInfo courseInfo = new CourseInfo(
                                    recordInfo.getSkuId(),
                                    recordInfo.getProductTitle(),
                                    recordInfo.getProductImg()
                            );
                            pSearchInfo.add(courseInfo);
                        }
                        pSearchState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pSearchState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqHotSearchList(int pageNumber) {
        pHotSearchState.setValue(LoadState.Loading);
        ProductModuleListReq productModuleListReq = new ProductModuleListReq(pageNumber);
        searchRepository.getProductModuleList(productModuleListReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ProductListMo>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProductListMo> response) {
                        assert(pageNumber > 0) : "热门搜索传入的pageNumber异常 小于等于0";
                        if (pageNumber == 1) pHotSearchResult.clear();
                        for (ProductListMo.Record record: response.data.getRecords()) {
                            CourseInfo courseInfo = new CourseInfo(
                                    record.getSkuId(),
                                    record.getProductTitle(),
                                    record.getProductImg()
                            );
                            pHotSearchResult.add(courseInfo);
                        }
                        pHotSearchState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pHotSearchState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getSearchHistory() {
        String historyStr = SPUtils.getInstance().getString(SharePrefer.searchHistory);
        if (historyStr != null && !historyStr.isEmpty()) {
            Gson gson = new Gson();
            mSearchHistoryList = gson.fromJson(historyStr, new TypeToken<List<String>>(){}.getType());
            pSearchHistory.setValue(LoadState.Success);
        } else {
            pSearchHistory.setValue(LoadState.Failed);
        }
        if (mSearchHistoryList == null) mSearchHistoryList = new ArrayList<>();
    }

    public void saveSearchHistory(String name) {
        mSearchHistoryList.remove(name);
        mSearchHistoryList.add(0, name);
        while (mSearchHistoryList.size() > Constant.SEARCH_HISTORY_MAX_NUM) {
            mSearchHistoryList.remove(mSearchHistoryList.size() - 1);
        }
        Gson gson = new Gson();
        SPUtils.getInstance().put(SharePrefer.searchHistory, gson.toJson(mSearchHistoryList));
    }

    public void clearSearchHistory() {
        mSearchHistoryList.clear();
        SPUtils.getInstance().remove(SharePrefer.searchHistory);
    }

    public String getHistoryItem(int position) {
        return mSearchHistoryList.get(position);
    }

    public int getHistoryNum() {
        if (mSearchHistoryList == null) {
            mSearchHistoryList = new ArrayList<>();
        }
        return mSearchHistoryList.size();
    }
}
