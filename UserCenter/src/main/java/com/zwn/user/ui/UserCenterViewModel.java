package com.zwn.user.ui;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.MineCommonMo;
import com.zeewain.base.model.MineHeader;
import com.zeewain.base.model.ReqState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zeewain.base.utils.SPUtils;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.data.model.FavoritesItem;
import com.zwn.user.data.model.HistoryItem;
import com.zwn.user.data.model.MessageCenterItem;
import com.zwn.user.data.protocol.request.DelFavoritesReq;
import com.zwn.user.data.protocol.request.DelHistoryReq;
import com.zwn.user.data.protocol.request.FavoritesReq;
import com.zwn.user.data.protocol.request.HistoryReq;
import com.zwn.user.data.protocol.response.AboutUsInfoResp;
import com.zwn.user.data.protocol.response.DelFavoritesResp;
import com.zwn.user.data.protocol.response.FavoritesResp;
import com.zwn.user.data.protocol.response.HistoryResp;
import com.zwn.user.utils.AndroidHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class UserCenterViewModel extends BaseViewModel {
    private final static String TAG = "UserCenterViewModel";

    public List<FavoritesItem> pFavoritesList = new ArrayList<>();
    public List<MessageCenterItem> pMsgList = new ArrayList<>();
    public List<HistoryItem> pHistoryList = new ArrayList<>();

    public List<DownloadInfo> pDownloadInfoList = new ArrayList<>();
    public AboutUsInfoResp pAboutUsInfo;

    public MutableLiveData<ReqState> pReqFavoritesState = new MutableLiveData<>();
    public MutableLiveData<ReqState> pDelFavoritesState = new MutableLiveData<>();

    public MutableLiveData<String> pReqMsgState = new MutableLiveData<>();
    public MutableLiveData<String> pDelMsgState = new MutableLiveData<>();

    public MutableLiveData<ReqState> pReqHistState = new MutableLiveData<>();
    public MutableLiveData<ReqState> pDelHistState = new MutableLiveData<>();
    public MutableLiveData<ReqState> pReqAboutUsInfoState = new MutableLiveData<>();

    private final UserRepository mUserRepository;
    private final Context mContext;

    private final String mTestUrl =
            "https://oss.zeewain.com/PRODUCT_IMAGE/49737859957544418076fa8cc1e41e2c/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20220111220913.jpg";

    public int pEnterPosition = -1;

    public UserCenterViewModel(UserRepository userRepository, Context context) {
        mUserRepository = userRepository;
        mContext = context;
    }

    /// --------------------------- 我的收藏 START ---------------------------
    public void reqFavoritesList() {
        if (CommonVariableCacheUtils.getInstance().token.isEmpty()) {
            pReqFavoritesState.setValue(ReqState.NEED_LOGIN);
            return;
        }
        final boolean needCheck;
        final String favoriteId;
        if (pEnterPosition != -1 && pFavoritesList.size() > 0) {
            needCheck = true;
            favoriteId = pFavoritesList.get(pEnterPosition).favoriteId;
        } else {
            needCheck = false;
            favoriteId = "";
        }
        mUserRepository.getUserFavorites(new FavoritesReq())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<List<FavoritesResp>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<FavoritesResp>> resp) {
                        if (resp.data != null) {
                            int localSize = pFavoritesList.size();
                            int respSize = resp.data.size();
                            boolean needReInit = true;
                            boolean hasMoveItem = false;
                            if (needCheck && localSize != 0) {
                                if (localSize == respSize + 1) {
                                    pFavoritesList.remove(pEnterPosition);
                                    int differentCount = 0;
                                    for (int i = 0; i < respSize; i++) {
                                        if (!pFavoritesList.get(i).favoriteId.equals(resp.data.get(i).favoriteId)) {
                                            differentCount++;
                                        }
                                    }
                                    if (differentCount == 0) {
                                        needReInit = false;
                                        pDelFavoritesState.setValue(ReqState.SUCCESS);
                                    }
                                } else if (localSize == respSize) {
                                    int hasChangeCount = 0;
                                    if (pFavoritesList.get(pEnterPosition).objId.equals(resp.data.get(0).objId)) {
                                        pFavoritesList.remove(pEnterPosition);
                                        FavoritesItem item = new FavoritesItem(
                                                resp.data.get(0).objUrl,
                                                resp.data.get(0).objName,
                                                resp.data.get(0).objDesc == null
                                                        ? "" : resp.data.get(0).objDesc,
                                                resp.data.get(0).favoriteId,
                                                resp.data.get(0).objId,
                                                AndroidHelper.str2Date(resp.data.get(0).favoriteTime));
                                        pFavoritesList.add(0, item);
                                        hasMoveItem = true;
                                    }
                                    for (int i = 0; i < respSize; i++) {
                                        if (pFavoritesList.get(i).objId.equals(resp.data.get(i).objId)) {
                                            pFavoritesList.get(i).favoriteId = resp.data.get(i).favoriteId;
                                        } else {
                                            hasChangeCount++;
                                        }
                                    }
                                    if (hasChangeCount == 0) {
                                        needReInit = false;
                                        pFavoritesList.get(pEnterPosition).favoriteId = resp.data.get(pEnterPosition).favoriteId;
                                    }
                                }
                            }
                            if (needReInit) {
                                pFavoritesList.clear();
                                for (int i = 0; i < resp.data.size(); i++) {
                                    FavoritesItem item = new FavoritesItem(
                                            resp.data.get(i).objUrl,
                                            resp.data.get(i).objName,
                                            resp.data.get(i).objDesc == null
                                                    ? "" : resp.data.get(i).objDesc,
                                            resp.data.get(i).favoriteId,
                                            resp.data.get(i).objId,
                                            AndroidHelper.str2Date(resp.data.get(i).favoriteTime));
                                    pFavoritesList.add(item);
                                }
                                pReqFavoritesState.setValue(ReqState.SUCCESS);
                            } else if (hasMoveItem) {
                                pDelFavoritesState.setValue(ReqState.SUCCESS);
                            }
                        } else {
                            if (resp.message != null) {
                                Toast.makeText(mContext, resp.message, Toast.LENGTH_SHORT).show();
                            }
                            pReqFavoritesState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (needCheck) {
                            Toast.makeText(mContext, "数据请求失败，网络或服务器异常", Toast.LENGTH_SHORT).show();
                        }
                        pReqFavoritesState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void delFavorites(int position) {
        List<String> idList = new ArrayList<>();
        idList.add(pFavoritesList.get(position).favoriteId);
        DelFavoritesReq delFavoritesReq = new DelFavoritesReq(idList);
        mUserRepository.delFavorites(delFavoritesReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DelFavoritesResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DelFavoritesResp> resp) {
                        if (resp.code == 0) {
                            pFavoritesList.remove(position);
                            pDelFavoritesState.setValue(ReqState.SUCCESS);
                        } else {
                            if (resp.message != null) {
                                Toast.makeText(mContext, resp.message, Toast.LENGTH_SHORT).show();
                            }
                            pDelFavoritesState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(mContext, "删除失败，网络或服务器异常", Toast.LENGTH_SHORT).show();
                        pDelFavoritesState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void clearFavorites() {
        List<String> idList = new ArrayList<>();
        for (FavoritesItem item: pFavoritesList) {
            idList.add(item.favoriteId);
        }
        DelFavoritesReq delFavoritesReq = new DelFavoritesReq(idList);
        mUserRepository.delFavorites(delFavoritesReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DelFavoritesResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DelFavoritesResp> resp) {
                        if (resp.code == 0) {
                            pFavoritesList.clear();
                            pDelFavoritesState.setValue(ReqState.SUCCESS);
                        } else {
                            if (resp.message != null) {
                                Toast.makeText(mContext, resp.message, Toast.LENGTH_SHORT).show();
                            }
                            pDelFavoritesState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(mContext, "清空失败，网络或服务器异常", Toast.LENGTH_SHORT).show();
                        pDelFavoritesState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    /// --------------------------- 我的收藏 END ---------------------------

    /// --------------------------- 消息中心 START ---------------------------
    public void getMessage() {
        for (int i = 0; i < 8; i++) {
            MessageCenterItem item = new MessageCenterItem(
                mTestUrl,
                "课件名称",
                "课件介绍课件介绍课件介绍课件介绍\n" +
                        "课件介绍最多两行",
                "2022.03.02"
            );
            pMsgList.add(item);
        }
        pReqMsgState.setValue(UserCenterConf.REQ_SUCCESS);
    }

    public void delMessage(int position) {
        pMsgList.remove(position);
        pDelMsgState.setValue(UserCenterConf.REQ_SUCCESS);
    }

    public void clearMessage() {
        pMsgList.clear();
        pDelMsgState.setValue(UserCenterConf.REQ_SUCCESS);
    }
    /// --------------------------- 消息中心 END ---------------------------

    /// --------------------------- 互动记录 START ---------------------------
    public void reqHistory() {
        if (CommonVariableCacheUtils.getInstance().token.isEmpty()) {
            pReqHistState.setValue(ReqState.NEED_LOGIN);
            return;
        }
        final String recordId;
        final boolean needCheck;
        if (pEnterPosition != -1 && pHistoryList.size() > 0) {
            needCheck = true;
            recordId = pHistoryList.get(pEnterPosition).recordId;
        } else {
            recordId = "";
            needCheck = false;
        }
        HistoryReq historyReq = new HistoryReq();
        mUserRepository.getUserHistory(historyReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<HistoryResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<HistoryResp> resp) {
                        if (resp.data != null) {
                            pHistoryList.clear();
                            for (Map<String, String> productInfo: resp.data.records) {
                                HistoryItem item = new HistoryItem(
                                        productInfo.get("productImage"),
                                        productInfo.get("productName"),
                                        productInfo.get("skuId"),
                                        productInfo.get("recordId")
                                );
                                pHistoryList.add(item);
                            }
                            if (needCheck && !pHistoryList.get(0).recordId.equals(recordId)) {
                                pReqHistState.setValue(ReqState.ERROR);
                            } else {
                                pReqHistState.setValue(ReqState.SUCCESS);
                            }
                        } else {
                            if (resp.message != null && !needCheck) {
                                Toast.makeText(mContext, resp.message, Toast.LENGTH_SHORT).show();
                            }
                            pReqHistState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pReqHistState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void delHistory(int position) {
        DelHistoryReq delHistoryReq = new DelHistoryReq(pHistoryList.get(position).recordId);
        mUserRepository.delUserHistory(delHistoryReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == 0) {
                            pHistoryList.remove(position);
                            pDelHistState.setValue(ReqState.SUCCESS);
                        } else {
                            if (resp.message != null) {
                                Toast.makeText(mContext, resp.message, Toast.LENGTH_SHORT).show();
                            }
                            pDelHistState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(mContext, "删除失败，网络或服务器异常", Toast.LENGTH_SHORT).show();
                        pDelHistState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void clearHistory() {
        mUserRepository.clearUserHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == 0) {
                            pHistoryList.clear();
                            pDelHistState.setValue(ReqState.SUCCESS);
                        } else {
                            if (resp.message != null) {
                                Toast.makeText(mContext, resp.message, Toast.LENGTH_SHORT).show();
                            }
                            pDelHistState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(mContext, "清除失败，网络或服务器异常", Toast.LENGTH_SHORT).show();
                        pDelHistState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    /// --------------------------- 互动记录 END ---------------------------

    public void getDownloadList() {
        List<DownloadInfo> allDownloadInfo = CareController.instance.getAllDownloadInfo(
                "type=" + BaseConstants.DownloadFileType.PLUGIN_APP);
        if (!CommonUtils.isUserLogin()) {
            return;
        }
        if ((allDownloadInfo != null) && (allDownloadInfo.size() > 0)) {
            for (int i = 0; i < allDownloadInfo.size(); i++) {
                if (allDownloadInfo.get(i).status == DownloadInfo.STATUS_SUCCESS) {
                    pDownloadInfoList.add(allDownloadInfo.get(i));
                }
            }
        }
    }

    public int delDownload(int position) {
        File file = new File(pDownloadInfoList.get(position).filePath);
        if (file.exists()) {
            file.delete();
        }
        return CareController.instance.deleteDownloadInfo(pDownloadInfoList.get(position).fileId);
    }

    public int clearDownload() {
        for (int i = 0; i < pDownloadInfoList.size(); i++) {
            if (delDownload(i) <= 0) {
                return i + 1;
            }
        }
        return 0;
    }

    public void reqAboutUsInfo() {
        mUserRepository.getAboutUsInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<AboutUsInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<AboutUsInfoResp> resp) {
                        if (resp.code == 0) {
                            pAboutUsInfo = resp.data;
                            pReqAboutUsInfoState.setValue(ReqState.SUCCESS);
                        } else {
                            Toast.makeText(mContext, resp.message, Toast.LENGTH_SHORT).show();
                            pReqAboutUsInfoState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pReqAboutUsInfoState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}