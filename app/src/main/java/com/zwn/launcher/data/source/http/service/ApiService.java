package com.zwn.launcher.data.source.http.service;


import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zwn.launcher.data.model.MainCategoryMo;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.data.protocol.request.CoursewareStartReq;
import com.zwn.launcher.data.protocol.request.MainCategoryReq;
import com.zwn.launcher.data.protocol.request.ProductListReq;
import com.zwn.launcher.data.protocol.request.ProductModuleListReq;
import com.zwn.launcher.data.protocol.response.FavoriteStateResp;
import com.zwn.user.data.protocol.request.CollectReq;
import com.zwn.user.data.protocol.request.FavoritesReq;
import com.zwn.launcher.data.protocol.request.ProDetailReq;
import com.zwn.launcher.data.protocol.request.PublishReq;
import com.zwn.user.data.protocol.request.RemoveCollectReq;
import com.zwn.launcher.data.protocol.request.UpgradeReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zwn.user.data.protocol.response.CollectResp;
import com.zwn.user.data.protocol.response.FavoritesResp;
import com.zwn.launcher.data.protocol.response.ProDetailResp;
import com.zwn.launcher.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.UpgradeResp;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST(BaseConstants.basePath + "/product/v2/category/list")
    Observable<BaseResp<List<MainCategoryMo>>> getMainCategoryList(@Body MainCategoryReq mainCategoryReq);

    @POST(BaseConstants.basePath + "/product/online/list")
    Observable<BaseResp<ProductListMo>> getProductList(@Body ProductListReq productListReq);

    @POST(BaseConstants.basePath + "/product/online/module")
    Observable<BaseResp<ProductListMo>> getProductModuleList(@Body ProductModuleListReq productModuleListReq);

    @POST(BaseConstants.basePath + "/product/online/detail")
    Observable<BaseResp<ProDetailResp>> getProDetailInfo(@Body ProDetailReq proDetailReq);

    @POST(BaseConstants.basePath + "/software/version/latest-published")
    Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq);

    @POST(BaseConstants.basePath + "/software/version/newer-published")
    Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq);

    @POST(BaseConstants.basePath + "/usercentre/favorites/list")
    Observable<BaseResp<List<FavoritesResp>>> getUserFavorites(@Body FavoritesReq favoritesReq);

    @GET(BaseConstants.basePath + "/usercentre/favorites/courseware/info?")
    Observable<BaseResp<FavoriteStateResp>> getFavoriteState(@Query("objId") String objId);

    @POST(BaseConstants.basePath + "/usercentre/favorites/courseware/add")
    Observable<BaseResp<CollectResp>> addFavorites(@Body CollectReq collectReq);

    @POST(BaseConstants.basePath + "/usercentre/favorites/courseware/del")
    Observable<BaseResp<String>> removeFavorites(@Body RemoveCollectReq removeCollectReq);

    @POST(BaseConstants.basePath + "/logcollection/log/upload")
    Observable<BaseResp<String>> uploadLog(@Body UploadLogReq uploadLogReq);

    @POST(BaseConstants.basePath + "/product/courseware/start")
    Observable<BaseResp<String>> startCourseware(@Body CoursewareStartReq coursewareStartReq);
}
