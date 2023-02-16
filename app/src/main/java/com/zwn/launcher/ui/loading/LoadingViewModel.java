package com.zwn.launcher.ui.loading;

import androidx.annotation.NonNull;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.ui.BaseViewModel;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.protocol.request.CoursewareStartReq;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoadingViewModel extends BaseViewModel {

    private final DataRepository dataRepository;

    public LoadingViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqStartCourseware(final String skuId){
        CoursewareStartReq coursewareStartReq = new CoursewareStartReq(skuId, "");
        dataRepository.startCourseware(coursewareStartReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


}
