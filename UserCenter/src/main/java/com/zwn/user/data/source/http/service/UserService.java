package com.zwn.user.data.source.http.service;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zwn.user.data.protocol.request.AkSkReq;
import com.zwn.user.data.protocol.request.DelFavoritesReq;
import com.zwn.user.data.protocol.request.DelHistoryReq;
import com.zwn.user.data.protocol.request.FavoritesReq;
import com.zwn.user.data.protocol.request.HistoryReq;
import com.zwn.user.data.protocol.request.MsgCodeReq;
import com.zwn.user.data.protocol.request.MsgLoginReq;
import com.zwn.user.data.protocol.request.PwdLoginReq;
import com.zwn.user.data.protocol.response.AboutUsInfoResp;
import com.zwn.user.data.protocol.response.AkSkResp;
import com.zwn.user.data.protocol.response.DelFavoritesResp;
import com.zwn.user.data.protocol.response.FavoritesResp;
import com.zwn.user.data.protocol.response.HistoryResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.protocol.response.MsgCodeResp;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {

    @POST(BaseConstants.basePath + "/auth/client/get-ak-sk")
    Observable<BaseResp<AkSkResp>> getAkSkInfo(@Body AkSkReq akSkReq);

    @POST(BaseConstants.basePath + "/captcha/captcha/sms")
    Observable<BaseResp<MsgCodeResp>> getMsgCode(@Body MsgCodeReq msgCodeReq);

    @POST(BaseConstants.basePath + "/sso/sso/login")
    Observable<BaseResp<LoginResp>> msgLogin(@Body MsgLoginReq msgLoginReq);

    @POST(BaseConstants.basePath + "/sso/sso/login")
    Observable<BaseResp<LoginResp>> pwdLogin(@Body PwdLoginReq pwdLoginReq);

    @POST(BaseConstants.basePath + "/usercentre/favorites/list")
    Observable<BaseResp<List<FavoritesResp>>> getUserFavorites(@Body FavoritesReq favoritesReq);

    @POST(BaseConstants.basePath + "/usercentre/favorites/del")
    Observable<BaseResp<DelFavoritesResp>> delFavorites(@Body DelFavoritesReq delFavoritesReq);

    @POST(BaseConstants.basePath + "/product/use/record/list")
    Observable<BaseResp<HistoryResp>> getUserHistory(@Body HistoryReq historyReq);

    @POST(BaseConstants.basePath + "/product/use/record/delete")
    Observable<BaseResp<String>> delUserHistory(@Body DelHistoryReq delHistoryReq);

    @POST(BaseConstants.basePath + "/product/use/record/clean")
    Observable<BaseResp<String>> clearUserHistory();

    @POST(BaseConstants.basePath + "/manager/aboutUs/info")
    Observable<BaseResp<AboutUsInfoResp>> getAboutUsInfo();
}
