package com.zwn.user.data;

import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zeewain.base.utils.SPUtils;
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
import com.zwn.user.data.protocol.response.MsgCodeResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.source.http.service.UserService;

import java.util.List;

import io.reactivex.Observable;

public class UserRepository {

    private static volatile UserRepository instance;
    private final UserService userService;

    private UserRepository(UserService userService){
        this.userService = userService;
    }

    public static UserRepository getInstance(){
        if(instance == null){
            synchronized (UserRepository.class) {
                if (instance == null){
                    instance = new UserRepository(RetrofitClient.getInstance().create(UserService.class));
                }
            }
        }
        return instance;
    }

    public Observable<BaseResp<AkSkResp>> getAkSkInfo(AkSkReq akSkReq){
        return userService.getAkSkInfo(akSkReq);
    }

    public Observable<BaseResp<MsgCodeResp>> getMsgCode(MsgCodeReq msgCodeReq) {
        return userService.getMsgCode(msgCodeReq);
    }

    public Observable<BaseResp<LoginResp>> msgLogin(MsgLoginReq msgLoginReq) {
        return userService.msgLogin(msgLoginReq);
    }

    public Observable<BaseResp<LoginResp>> pwdLogin(PwdLoginReq pwdLoginReq) {
        return userService.pwdLogin(pwdLoginReq);
    }

    public Observable<BaseResp<List<FavoritesResp>>> getUserFavorites(FavoritesReq favoritesReq) {
        return userService.getUserFavorites(favoritesReq);
    }

    public Observable<BaseResp<DelFavoritesResp>> delFavorites(DelFavoritesReq delFavoritesReq) {
        return userService.delFavorites(delFavoritesReq);
    }

    public Observable<BaseResp<HistoryResp>> getUserHistory(HistoryReq historyReq) {
        return userService.getUserHistory(historyReq);
    }

    public Observable<BaseResp<String>> delUserHistory(DelHistoryReq delHistoryReq) {
        return userService.delUserHistory(delHistoryReq);
    }

    public Observable<BaseResp<String>> clearUserHistory() {
        return userService.clearUserHistory();
    }

    public Observable<BaseResp<AboutUsInfoResp>> getAboutUsInfo() {
        return userService.getAboutUsInfo();
    }

    public void putValue(String key, String value) {
        SPUtils.getInstance().put(key, value);
    }

    public String getString(String key) {
        return SPUtils.getInstance().getString(key);
    }

    public void removeValue(String key) {
        SPUtils.getInstance().remove(key);
    }
}
