package com.zwn.launcher;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.LogFileConfig;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.SPUtils;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.model.MainCategoryMo;
import com.zwn.launcher.data.model.PagedModuleListLoadState;
import com.zwn.launcher.data.model.PagedPrdListLoadState;
import com.zwn.launcher.data.model.ProductListLoadState;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.data.protocol.request.MainCategoryReq;
import com.zwn.launcher.data.protocol.request.ProductListReq;
import com.zwn.launcher.data.protocol.request.ProductModuleListReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zwn.launcher.data.protocol.request.UpgradeReq;
import com.zwn.launcher.data.protocol.response.UpgradeResp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {

    private static final String TAG = MainViewModel.class.getName();

    private final DataRepository dataRepository;

    public List<MainCategoryMo> mainCategoryList;

    public MutableLiveData<LoadState> mldMainCategoryListLoadState = new MutableLiveData<>();

    public MutableLiveData<ProductListLoadState> mldInitDataLoadState = new MutableLiveData<>();
    public MutableLiveData<PagedPrdListLoadState> mldProductListLoadState = new MutableLiveData<>();
    public MutableLiveData<ProductListLoadState> mldProductModuleListLoadState = new MutableLiveData<>();

    public MutableLiveData<ProductListLoadState> mldCareModuleListLoadState = new MutableLiveData<>();
    public MutableLiveData<PagedModuleListLoadState> mldCareModuleListPagedLoadState = new MutableLiveData<>();

    public MutableLiveData<Integer> mldSelectedTab = new MutableLiveData<>();
    public MutableLiveData<Boolean> mldNetConnected = new MutableLiveData<>();

    public MutableLiveData<LoadState> mHostAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mManagerAppUpgradeState = new MutableLiveData<>();
    public UpgradeResp hostAppUpgradeResp;
    public UpgradeResp managerAppUpgradeResp;

    private final ConcurrentHashMap<String, String> reqProductListMap = new ConcurrentHashMap<>();//used for filter req;
    private final ConcurrentHashMap<String, String> reqCareModuleListMap = new ConcurrentHashMap<>();//used for filter req;
    private final HashMap<String, ProductListMo> productListMap = new HashMap<>();
    private final HashMap<String, ProductListMo> productModuleListMap = new HashMap<>();
    private final HashMap<String, ProductListMo> careModuleListMap = new HashMap<>();
    private final AtomicInteger pendingPrepareCount = new AtomicInteger();

    public int tryReqMainCategoryTimes = 3;

    public MainViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        mainCategoryList = new ArrayList<>();
    }

    public void initCommonData(final String categoryId){
        if(productListMap.get(categoryId) != null && productModuleListMap.get(categoryId) != null){
            mldInitDataLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Success));
        }else{
            mldInitDataLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Loading));
            if(productListMap.get(categoryId) == null) {
                reqProductList(categoryId);
            }else{
                mldProductListLoadState.setValue(new PagedPrdListLoadState(categoryId, 1, LoadState.Success));
            }
            if(productModuleListMap.get(categoryId) == null) {
                reqProductModuleList(categoryId, ProdConstants.Module.TYPE_4);
            }else{
                mldProductModuleListLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Success));
            }
        }
    }

    public boolean isCommonDataDone(String categoryId){
        if(productListMap.get(categoryId) != null && productModuleListMap.get(categoryId) != null){
            return true;
        }
        return false;
    }

    public ProductListMo getProductListFromCache(String categoryId){
        return productListMap.get(categoryId);
    }

    public ProductListMo getProductModuleListFromCache(String categoryId){
        return productModuleListMap.get(categoryId);
    }

    public ProductListMo getCareModuleListFromCache(String moduleType){
        return careModuleListMap.get(moduleType);
    }

    public void reqMainCategoryList(){
        tryReqMainCategoryTimes--;
        mldMainCategoryListLoadState.setValue(LoadState.Loading);
        dataRepository.getMainCategoryList(new MainCategoryReq("1", ""))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<List<MainCategoryMo>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<MainCategoryMo>> response) {
                        if(response.data.size() > 0) {
                            mainCategoryList.clear();
                            mainCategoryList.add(new MainCategoryMo("我的", "我的"));
                            mainCategoryList.add(new MainCategoryMo("care", "精选"));
                        }
                        mainCategoryList.addAll(response.data);
                        mldMainCategoryListLoadState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldMainCategoryListLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqHostAppUpgrade(String version) {
        mHostAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.HOST_APP_SOFTWARE_CODE);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        hostAppUpgradeResp = response.data;
                        if(hostAppUpgradeResp != null){
                            if(hostAppUpgradeResp.getVersionId() == null || hostAppUpgradeResp.getVersionId().isEmpty()){
                                hostAppUpgradeResp = null;
                            }
                        }
                        mHostAppUpgradeState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mHostAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqManagerAppUpgrade(String version) {
        mManagerAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.MANAGER_APP_SOFTWARE_CODE);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        managerAppUpgradeResp = response.data;
                        if(managerAppUpgradeResp != null){
                            if(managerAppUpgradeResp.getVersionId() == null || managerAppUpgradeResp.getVersionId().isEmpty()){
                                managerAppUpgradeResp = null;
                            }
                        }
                        mManagerAppUpgradeState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mManagerAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public int getTotalRecordSize(String categoryId){
        final ProductListMo productListMo = productListMap.get(categoryId);
        if(productListMo != null){
            return productListMo.getTotal();
        }
        return 0;
    }

    public int getCareModuleTotalRecordSize(String moduleType){
        final ProductListMo productListMo = careModuleListMap.get(moduleType);
        if(productListMo != null){
            return productListMo.getTotal();
        }
        return 0;
    }

    public void reqProductList(final String categoryId){
        if(reqProductListMap.containsKey(categoryId)) return;
        reqProductListMap.put(categoryId, categoryId);

        final ProductListMo productListMo = productListMap.get(categoryId);
        int pageNum = 1;
        if(productListMo != null){
            int modPage = productListMo.getRecords().size() % ProdConstants.PRD_PAGE_SIZE;
            if(modPage > 0){// loaded Done;
                reqProductListMap.remove(categoryId);
                return;
            }
            pageNum = productListMo.getRecords().size() / ProdConstants.PRD_PAGE_SIZE + 1;
        }
        final int reqPageNum = pageNum;
        mldProductListLoadState.setValue(new PagedPrdListLoadState(categoryId, reqPageNum, LoadState.Loading));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ProductListReq productListReq = new ProductListReq(categoryId, true, reqPageNum, ProdConstants.PRD_PAGE_SIZE);
                dataRepository.getProductList(productListReq)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(MainViewModel.this)
                        .subscribe(new DisposableObserver<BaseResp<ProductListMo>>() {
                            @Override
                            public void onNext(@NonNull BaseResp<ProductListMo> response) {
                                reqProductListMap.remove(categoryId);
                                ProductListMo productListMo = productListMap.get(categoryId);
                                if(productListMo == null){
                                    productListMap.put(categoryId, response.data);
                                }else {
                                    productListMo.getRecords().addAll(response.data.getRecords());
                                }
                                mldProductListLoadState.setValue(new PagedPrdListLoadState(categoryId, reqPageNum, LoadState.Success));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                reqProductListMap.remove(categoryId);
                                mldProductListLoadState.setValue(new PagedPrdListLoadState(categoryId, reqPageNum, LoadState.Failed));
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        }, (reqPageNum > 1) ? 600 : 0);

    }

    public void reqProductModuleList(final String categoryId, final String moduleType){
        mldProductModuleListLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Loading));
        ProductModuleListReq productModuleListReq = new ProductModuleListReq(categoryId, moduleType, true, 1, 5);
        dataRepository.getProductModuleList(productModuleListReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ProductListMo>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProductListMo> response) {
                        productModuleListMap.put(categoryId, response.data);
                        mldProductModuleListLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Success));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldProductModuleListLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void initCareModuleList(final String categoryId){
        mldCareModuleListLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Loading));
        careModuleListMap.clear();
        pendingPrepareCount.set(3);
        reqCareModuleList(categoryId, ProdConstants.Module.TYPE_5);
        reqCareModuleList(categoryId, ProdConstants.Module.TYPE_2);
        reqCareModuleList(categoryId, ProdConstants.Module.TYPE_7);
    }

    private void decrementCountAndCheck(final String categoryId){
        int newPendingCount = pendingPrepareCount.decrementAndGet();
        if(newPendingCount <= 0){
            if(careModuleListMap.size() == 3){
                mldCareModuleListLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Success));
            }else{
                mldCareModuleListLoadState.setValue(new ProductListLoadState(categoryId, LoadState.Failed));
            }
        }
    }

    private void reqCareModuleList(final String categoryId, final String moduleType){
        ProductModuleListReq productModuleListReq = new ProductModuleListReq("",
                moduleType, true, 1, ProdConstants.PRD_PAGE_SIZE);
        dataRepository.getProductModuleList(productModuleListReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ProductListMo>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProductListMo> response) {
                        careModuleListMap.put(moduleType, response.data);
                        decrementCountAndCheck(categoryId);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        decrementCountAndCheck(categoryId);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqCareModuleListPaged(final String categoryId, final String moduleType){
        if(reqCareModuleListMap.containsKey(moduleType)) return;
        reqCareModuleListMap.put(moduleType, moduleType);

        final ProductListMo productListMo = careModuleListMap.get(moduleType);
        int pageNum = 1;
        if(productListMo != null){
            int modPage = productListMo.getRecords().size() % ProdConstants.PRD_PAGE_SIZE;
            if(modPage > 0){// loaded Done;
                reqCareModuleListMap.remove(moduleType);
                return;
            }
            pageNum = productListMo.getRecords().size() / ProdConstants.PRD_PAGE_SIZE + 1;
        }
        final int reqPageNum = pageNum;
        mldCareModuleListPagedLoadState.setValue(new PagedModuleListLoadState(categoryId, moduleType, reqPageNum, LoadState.Loading));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ProductModuleListReq productModuleListReq = new ProductModuleListReq("",
                        moduleType, true, reqPageNum, ProdConstants.PRD_PAGE_SIZE);
                dataRepository.getProductModuleList(productModuleListReq)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(MainViewModel.this)
                        .subscribe(new DisposableObserver<BaseResp<ProductListMo>>() {
                            @Override
                            public void onNext(@NonNull BaseResp<ProductListMo> response) {
                                reqCareModuleListMap.remove(moduleType);
                                ProductListMo productListMo = careModuleListMap.get(moduleType);
                                if (productListMo == null) {
                                    careModuleListMap.put(moduleType, response.data);
                                } else {
                                    productListMo.getRecords().addAll(response.data.getRecords());
                                }
                                mldCareModuleListPagedLoadState.setValue(new PagedModuleListLoadState(categoryId, moduleType, reqPageNum, LoadState.Success));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                reqCareModuleListMap.remove(moduleType);
                                mldCareModuleListPagedLoadState.setValue(new PagedModuleListLoadState(categoryId, moduleType, reqPageNum, LoadState.Failed));
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
       }, 600);
    }

    private void uploadLog(String time, String message) {
        UploadLogReq uploadLogReq = new UploadLogReq(message, time);
        dataRepository.uploadLog(uploadLogReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new ResourceObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == 0) {
                            SPUtils.getInstance().put(SharePrefer.upLoadLogFile, "");
                            Log.w(TAG, "日志上传成功！");
                        } else {
                            Log.e(TAG, resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "日志上传错误！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void checkCrashLog() {
        List<File> fileList = FileUtils.getFiles(LogFileConfig.directoryPath);
        if (fileList != null && fileList.size() > LogFileConfig.maxSize) {
            for (int i = fileList.size() - 1; i >= LogFileConfig.maxSize; i--) {
                String deleteFilePath = fileList.get(i).getAbsolutePath();
                boolean deleteResult = FileUtils.deleteFile(deleteFilePath);
                if (!deleteResult) {
                    Log.e(TAG, fileList.get(i).getAbsolutePath() + "删除失败");
                }
            }
        }

        String logFileName = SPUtils.getInstance().getString(SharePrefer.upLoadLogFile);
        if (logFileName == null || logFileName.isEmpty() || logFileName.equals("null")) {
            return;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = dateFormat.format(new Date());
        String logPath = LogFileConfig.directoryPath + logFileName;
        String logMessage = FileUtils.readFile(logPath);
        if (logMessage != null) {
            uploadLog(time, "STATE=重启后\n" + logMessage);
        }
    }
}
