package com.zwn.launcher.data;


import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zwn.launcher.data.model.MainCategoryMo;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.data.protocol.request.CoursewareStartReq;
import com.zwn.launcher.data.protocol.request.MainCategoryReq;
import com.zwn.launcher.data.protocol.request.ProductListReq;
import com.zwn.launcher.data.protocol.request.ProductModuleListReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zwn.launcher.data.protocol.response.FavoriteStateResp;
import com.zwn.launcher.data.source.http.service.ApiService;
import com.zwn.user.data.protocol.request.CollectReq;
import com.zwn.user.data.protocol.request.FavoritesReq;
import com.zwn.launcher.data.protocol.request.ProDetailReq;
import com.zwn.launcher.data.protocol.request.PublishReq;
import com.zwn.user.data.protocol.request.RemoveCollectReq;
import com.zwn.launcher.data.protocol.request.UpgradeReq;
import com.zwn.user.data.protocol.response.CollectResp;
import com.zwn.user.data.protocol.response.FavoritesResp;
import com.zwn.launcher.data.protocol.response.ProDetailResp;
import com.zwn.launcher.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.UpgradeResp;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;

public class DataRepository {

    private static volatile DataRepository instance;
    private final ApiService apiService;

    private DataRepository(ApiService apiService){
        this.apiService = apiService;
    }

    public static DataRepository getInstance(){
        if(instance == null){
            synchronized (DataRepository.class){
                if (instance == null){
                    instance = new DataRepository(RetrofitClient.getInstance().create(ApiService.class));
                }
            }
        }
        return instance;
    }

    public Observable<BaseResp<List<MainCategoryMo>>> getMainCategoryList(MainCategoryReq mainCategoryReq){
        return apiService.getMainCategoryList(mainCategoryReq);
    }

    public Observable<BaseResp<ProductListMo>> getProductList(ProductListReq productListReq){
        return apiService.getProductList(productListReq);
    }

    public Observable<BaseResp<ProductListMo>> getProductModuleList(ProductModuleListReq productModuleListReq){
        return apiService.getProductModuleList(productModuleListReq);
    }

    public Observable<BaseResp<ProDetailResp>> getProDetailInfo(@Body ProDetailReq proDetailReq){
        return apiService.getProDetailInfo(proDetailReq);
    }

    public Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq){
        return apiService.getPublishedVersionInfo(publishReq);
    }

    public Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq){
        return apiService.getUpgradeVersionInfo(upgradeReq);
    }

    public Observable<BaseResp<FavoriteStateResp>> getFavoriteState(String objId) {
        return  apiService.getFavoriteState(objId);
    }

    public Observable<BaseResp<List<FavoritesResp>>> getUserFavorites(FavoritesReq favoritesReq) {
        return apiService.getUserFavorites(favoritesReq);
    }

    public Observable<BaseResp<CollectResp>> addFavorites(@Body CollectReq collectReq){
        return apiService.addFavorites(collectReq);
    }

    public  Observable<BaseResp<String>> removeFavorites(@Body RemoveCollectReq removeCollectReq){
        return apiService.removeFavorites(removeCollectReq);
    }

    public Observable<BaseResp<String>> uploadLog(UploadLogReq uploadLogReq) {
        return apiService.uploadLog(uploadLogReq);
    }

    public Observable<BaseResp<String>> startCourseware(CoursewareStartReq coursewareStartReq){
        return apiService.startCourseware(coursewareStartReq);
    }
}
