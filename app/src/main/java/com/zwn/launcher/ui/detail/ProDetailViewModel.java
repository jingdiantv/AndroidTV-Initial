package com.zwn.launcher.ui.detail;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.protocol.response.FavoriteStateResp;
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

import java.util.ArrayList;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ProDetailViewModel extends BaseViewModel {

    private static final String TAG = "ProDetailViewModel";

    private final DataRepository dataRepository;

    public MutableLiveData<LoadState> mDetailLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mPublishState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mUpgradeState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mCollectListState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mAddCollectState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mRemoveCollectState = new MutableLiveData<>();
    public ProDetailResp proDetailResp;
    public PublishResp publishResp;
    public UpgradeResp upgradeResp;
    public CollectResp collectResp;
//    public List<FavoritesResp> favoritesList = new ArrayList<>();

    public ProDetailViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void getProDetailInfo(ProDetailReq proDetailReq) {
        mDetailLoadState.setValue(LoadState.Loading);
        dataRepository.getProDetailInfo(proDetailReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ProDetailResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProDetailResp> response) {
                        proDetailResp = response.data;
                        mDetailLoadState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mDetailLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getPublishVersionInfo(PublishReq publishReq) {
        mPublishState.setValue(LoadState.Loading);
        dataRepository.getPublishedVersionInfo(publishReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<PublishResp> response) {
                        publishResp = response.data;
                        mPublishState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mPublishState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getUpgradeVersionInfo(UpgradeReq upgradeReq) {
        mUpgradeState.setValue(LoadState.Loading);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        upgradeResp = response.data;
                        if(upgradeResp != null){
                            if(upgradeResp.getVersionId() == null || upgradeResp.getVersionId().isEmpty()){
                                upgradeResp = null;
                            }
                        }
                        mUpgradeState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqFavoriteState(String skuId) {
        dataRepository.getFavoriteState(skuId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<FavoriteStateResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<FavoriteStateResp> resp) {
                        if (resp.code == 0 && resp.data != null && resp.data.getObjId() != null) {
                            mCollectListState.setValue(LoadState.Success);
                        } else {
                            Log.d(TAG, resp.message);
                            mCollectListState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mCollectListState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

//    public void reqFavoritesList() {
//        mCollectListState.setValue(LoadState.Loading);
//        dataRepository.getUserFavorites(new FavoritesReq())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe(this)
//                .subscribe(new DisposableObserver<BaseResp<List<FavoritesResp>>>() {
//                    @Override
//                    public void onNext(@NonNull BaseResp<List<FavoritesResp>> response) {
//                        favoritesList = response.data;
//                        mCollectListState.setValue(LoadState.Success);
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        mCollectListState.setValue(LoadState.Failed);
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }

    public void addFavorites(CollectReq collectReq) {
        mAddCollectState.setValue(LoadState.Loading);
        dataRepository.addFavorites(collectReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<CollectResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<CollectResp> response) {
                        collectResp = response.data;
                        mAddCollectState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mAddCollectState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void removeFavorites(RemoveCollectReq removeCollectReq) {
        mRemoveCollectState.setValue(LoadState.Loading);
        dataRepository.removeFavorites(removeCollectReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {
                        mRemoveCollectState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mRemoveCollectState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}