package com.zwn.launcher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.FocusHighlightHelper;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.zeewain.base.Views.CustomProgressDialog;
import com.zeewain.base.Views.CustomUpgradeDialog;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.model.TopGroupAction;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.ui.OnTopGroupInteractionListener;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.search.ui.SearchActivity;
import com.zwn.launcher.adapter.ProductCategoryViewPagerAdapter;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.protocol.response.UpgradeResp;
import com.zwn.launcher.databinding.ActivityMainBinding;
import com.zwn.launcher.presenter.MainCategoryPresenter;
import com.zwn.launcher.ui.loading.LoadingPluginActivity;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.ui.LoginCenterActivity;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends BaseActivity implements  ViewTreeObserver.OnGlobalFocusChangeListener, OnTopGroupInteractionListener {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private ArrayObjectAdapter mArrayObjectAdapter;
    private int mCurrentPageIndex = 0;
    private MainViewModel mainViewModel;
    private ProductCategoryViewPagerAdapter productCategoryAdapter;
    private CareBroadcastReceiver careBroadcastReceiver;

    private boolean isSkipTabFromViewPager = false;
    private TextView mOldTitle;
    private boolean isFirstIn = true;
    private boolean isLoadCache = false;

    private final int REQUEST_CODE_PERMISSIONS = 1;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
    };

    private CustomProgressDialog progressDialog;
    private boolean isFromSetting;

    private static final ExecutorService mFixedPool = Executors.newFixedThreadPool(1);
    public static String installingFileId = null;
    public static final ConcurrentLinkedQueue<String> installingQueue = new ConcurrentLinkedQueue<>();

    private DownloadService.DownloadBinder downloadBinder = null;
    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            if (fileId.equals(BaseConstants.HOST_APP_SOFTWARE_CODE)) {
                runOnUiThread(() -> {
                    if (progress < 100) {
                        progressDialog.setTitle("正在更新");
                        progressDialog.setProgress(progress);
                    } else if (progress == 100) {
                        progressDialog.dismiss();
                    }
                });
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            if(BaseConstants.DownloadFileType.PLUGIN_APP == type || BaseConstants.DownloadFileType.MANAGER_APP == type){
                addToQueueNextCheckInstall(fileId);
            }else if(BaseConstants.DownloadFileType.HOST_APP == type){
                handleHostInstall(file.getPath(), fileId);
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(BaseConstants.HOST_APP_SOFTWARE_CODE)) {
                runOnUiThread(() -> progressDialog.setTitle("网络异常"));
            }
        }

        @Override
        public void onPaused(String fileId) {}

        @Override
        public void onCancelled(String fileId) {}

        @Override
        public void onUpdate(String fileId) {}
    };

    public static void addToQueueNextCheckInstall(String fileId){
        installingQueue.offer(fileId);
        handleCommonApkInstall();
    }

    public static boolean isInQueue(String fileId){
        return installingQueue.contains(fileId);
    }

    private synchronized static void handleCommonApkInstall(){
        if(installingFileId == null){
            installingFileId = installingQueue.poll();
            Log.i(TAG, "handleCommonApkInstall() prepare for installation fileId=" + installingFileId);
            if(installingFileId != null){
                final DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(installingFileId);
                if(downloadInfo != null){
                    mFixedPool.execute(() -> {
                        //used for third party app default enable all Permission
                        /*String pkgNames = SystemProperties.get(BaseConstants.PERSIST_SYS_PERMISSION_PKG);
                        if(!TextUtils.isEmpty(pkgNames)){
                            SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, pkgNames + ";" + downloadInfo.mainClassPath);
                        }else{
                            SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, downloadInfo.mainClassPath);
                        }*/
                        Intent intent = new Intent();
                        intent.setAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
                        intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, downloadInfo.fileId);
                        Log.i(TAG, "handleCommonApkInstall() install fileId = " + downloadInfo.fileId);
                        intent.putExtra(BaseConstants.EXTRA_PLUGIN_FILE_PATH, downloadInfo.filePath);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(ZeeApplication.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        IntentSender statusReceiver = pendingIntent.getIntentSender();
                        boolean success = ApkUtil.installApkSession(ZeeApplication.applicationContext, downloadInfo.filePath, statusReceiver);
                        if (!success) {
                            Log.e(TAG, "handleCommonApkInstall() failed to install " + installingFileId);
                            installingFileId = null;
                            handleCommonApkInstall();
                        }
                    });
                }else{
                    installingFileId = null;
                    handleCommonApkInstall();
                }
            }else{
                Log.i(TAG, "handleCommonApkInstall() install app done!");
            }
        } else{
            Log.i(TAG, "handleCommonApkInstall() fileId=" + installingFileId + " is installing!");
        }
    }

    private void handleHostInstall(String hostApkPath, String fileId){
        Intent intent = new Intent(BaseConstants.PACKAGE_INSTALLED_ACTION);
        ComponentName componentName = new ComponentName(BaseConstants.MANAGER_PACKAGE_NAME, BaseConstants.MANAGER_INSTALL_ACTIVITY);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BaseConstants.EXTRA_APK_PATH, hostApkPath);
        intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, fileId);
        startActivity(intent);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            if (downloadBinder != null) {
                downloadBinder.registerDownloadListener(downloadListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    public HorizontalGridView getHorizontalGridView() {
        return binding.horizontalGridViewMainCategory;
    }

    public Group getGroup() {
        return binding.idGroup;
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        Log.d(TAG, "onGlobalFocusChanged newFocus: " + newFocus);
        Log.d(TAG, "onGlobalFocusChanged oldFocus: " + oldFocus);
        if (newFocus == null) {
            return;
        }
        if(oldFocus != null){
            if (newFocus.getId() == R.id.cl_main_category_root
                    && oldFocus.getId() == R.id.cl_main_category_root) {
                View newView = newFocus.findViewById(R.id.bar_main_category_selected);
                newView.setVisibility(View.GONE);
                TextView textViewNew = newFocus.findViewById(R.id.tv_main_title);
                textViewNew.setTextColor(0xFF2A2C41);

                View oldView = oldFocus.findViewById(R.id.bar_main_category_selected);
                oldView.setVisibility(View.GONE);
                TextView textView = oldFocus.findViewById(R.id.tv_main_title);
                textView.setTextColor(0xFFE2E2E2);

            } else if (newFocus.getId() == R.id.cl_main_category_root
                    && oldFocus.getId() != R.id.cl_main_category_root) {
                View newView = newFocus.findViewById(R.id.bar_main_category_selected);
                newView.setVisibility(View.GONE);
                TextView textViewNew = newFocus.findViewById(R.id.tv_main_title);
                textViewNew.setTextColor(0xFF2A2C41);

            } else if (newFocus.getId() != R.id.cl_main_category_root
                    && oldFocus.getId() == R.id.cl_main_category_root) {
                View oldView = oldFocus.findViewById(R.id.bar_main_category_selected);
                oldView.setVisibility(View.VISIBLE);
                TextView textView = oldFocus.findViewById(R.id.tv_main_title);
                textView.setTextColor(0xFF6CEDFF);
            }

            if(newFocus.getId() == R.id.viewPager_product && oldFocus.getId() == R.id.cl_main_category_root){
                binding.horizontalGridViewMainCategory.requestFocus();
            }
        }else if(newFocus.getId() == R.id.cl_main_category_root){
            View newView = newFocus.findViewById(R.id.bar_main_category_selected);
            newView.setVisibility(View.GONE);
            TextView textViewNew = newFocus.findViewById(R.id.tv_main_title);
            textViewNew.setTextColor(0xFF2A2C41);
        }
    }

    private void bindService() {
        Intent bindIntent = new Intent(this.getApplicationContext(), DownloadService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void checkApkInstallResult(){
        String apkPath = getIntent().getStringExtra(BaseConstants.EXTRA_APK_PATH);
        if(apkPath != null && !apkPath.isEmpty()){
            boolean apkInstallResult = getIntent().getBooleanExtra(BaseConstants.EXTRA_APK_INSTALL_RESULT, false);
            String fileId = getIntent().getStringExtra(BaseConstants.EXTRA_PLUGIN_NAME);
            Log.i(TAG, "handleApkInstallResult() apkPath: " + apkPath + ", result=" + apkInstallResult + ", fileId=" + fileId);
            if(apkInstallResult){
                showToast("版本更新成功！");
            }
            if(fileId != null && CareController.instance.deleteDownloadInfo(fileId) > 0) {
                FileUtils.deleteFile(apkPath);
            }
        }
    }

    private void checkHostApkStats(){
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
        if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
            File file = new File(downloadInfo.filePath);
            if (!file.exists()) {
                CareController.instance.deleteDownloadInfo(BaseConstants.HOST_APP_SOFTWARE_CODE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        DensityUtils.autoWidth(getApplication(), this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Log.i(TAG, "new version: " + ApkUtil.getAppVersionName(this));
        checkApkInstallResult();
        bindService();
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(MainActivity.this);
            progressDialog.setTitle("正在更新");
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        }

        CommonVariableCacheUtils.getInstance().initToken();
        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        mainViewModel = ViewModelProviders.of(this, factory).get(MainViewModel.class);

        binding.horizontalGridViewMainCategory.setHorizontalSpacing(DisplayUtil.dip2px(this, 10));
        binding.horizontalGridViewMainCategory.setGravity(Gravity.CENTER);
        mArrayObjectAdapter = new ArrayObjectAdapter(new MainCategoryPresenter());
        ItemBridgeAdapter itemBridgeAdapter = new ItemBridgeAdapter(mArrayObjectAdapter);
        binding.horizontalGridViewMainCategory.setAdapter(itemBridgeAdapter);
        binding.horizontalGridViewMainCategory.setOnChildViewHolderSelectedListener(onChildViewHolderSelectedListener);
        FocusHighlightHelper.setupBrowseItemFocusHighlight(itemBridgeAdapter,
                FocusHighlight.ZOOM_FACTOR_NONE, false);

        binding.viewPagerProduct.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                if (isFirstIn) {
                    isFirstIn = false;
                } else {
                    isSkipTabFromViewPager = true;
                }
                if (position != mCurrentPageIndex) {
                    binding.horizontalGridViewMainCategory.setSelectedPosition(position);
                }
                mainViewModel.mldSelectedTab.setValue(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        mArrayObjectAdapter.addAll(0, mainViewModel.mainCategoryList);

        productCategoryAdapter = new ProductCategoryViewPagerAdapter(getSupportFragmentManager(), 0);
        binding.viewPagerProduct.setAdapter(productCategoryAdapter);
//        binding.viewPagerProduct.setOffscreenPageLimit(1);
        productCategoryAdapter.setDataList(mainViewModel.mainCategoryList);

        initListener();
        initViewObservable();
        registerBroadCast();

        if(!CommonUtils.createOrClearPluginModelDir()){
            showToast("模型目录创建失败！");
        }

        checkHostApkStats();

        if(NetworkUtil.isNetworkAvailable(this)) {
            mainViewModel.reqMainCategoryList();
            mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
        }else{
            binding.loadingViewMain.setVisibility(View.VISIBLE);
            binding.loadingViewMain.startAnim();
            binding.loadingViewMain.requestFocus();

            binding.loadingViewMain.setTag(true);
            binding.loadingViewMain.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkShouldInitData();
                }
            }, 5000);
        }

        setLauncherWallpaper();
    }

    private synchronized void checkShouldInitData(){
        if(binding.loadingViewMain.getTag() != null){
            boolean needLoadData = (Boolean)binding.loadingViewMain.getTag();
            binding.loadingViewMain.setTag(false);
            if(needLoadData){
                if(!NetworkUtil.isNetworkAvailable(this)){
                    isLoadCache = true;
                }
                mainViewModel.reqMainCategoryList();
            }
        }
    }

    private void setLauncherWallpaper(){
        boolean isSetWallpaperDone = SPUtils.getInstance().getBoolean(SharePrefer.SetWallpaperDone, false);
        if(!isSetWallpaperDone) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            try {
                wallpaperManager.setBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_black_bg));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                SPUtils.getInstance().put(SharePrefer.SetWallpaperDone, true);
            }
        }
    }


    private void initListener() {
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(this);
        binding.horizontalGridViewMainCategory.addOnChildViewHolderSelectedListener(onChildViewHolderSelectedListener);
        binding.itsvSearch.setOnClickListener(view -> {
            Intent intent=new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        binding.itsvSettings.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(view.getContext(), SettingsActivity.class);
            startActivity(intent);
        });
        binding.itsvUser.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(view.getContext(), LoginCenterActivity.class);
            startActivity(intent);
        });
        binding.itsvNotify.setOnClickListener(view -> {
            showToast(R.string.not_support_now);
            /*Intent intent = new Intent(view.getContext(), UserCenterActivity.class);
            intent.putExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC, UserCenterConf.FRAGMENT_MESSAGE_CENTER);
            startActivity(intent);*/
        });

        binding.itsvWifi.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            startActivity(intent);
        });

        binding.networkErrViewMain.setRetryClickListener(() -> {
            mainViewModel.tryReqMainCategoryTimes = 3;
            mainViewModel.reqMainCategoryList();
            if(LoadState.Failed == mainViewModel.mManagerAppUpgradeState.getValue()){
                mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
            }else if(LoadState.Failed == mainViewModel.mHostAppUpgradeState.getValue()){
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }
        });
    }

    private void updateCategorySelected(){
        if (mainViewModel.mainCategoryList.size() > ProdConstants.DEFAULT_FOCUS_INDEX) {
            binding.horizontalGridViewMainCategory.setSelectedPositionSmooth(ProdConstants.DEFAULT_FOCUS_INDEX);
            View positionView = binding.horizontalGridViewMainCategory.getChildAt(ProdConstants.DEFAULT_FOCUS_INDEX);
            if (positionView != null) {
                mOldTitle = positionView.findViewById(R.id.tv_main_title);
            }
        } else {
            binding.horizontalGridViewMainCategory.setSelectedPositionSmooth(0);
            View positionView = binding.horizontalGridViewMainCategory.getChildAt(0);
            if (positionView != null) {
                mOldTitle = positionView.findViewById(R.id.tv_main_title);
            }
        }
    }

    private void initViewObservable() {
        mainViewModel.mldMainCategoryListLoadState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                binding.loadingViewMain.setVisibility(View.GONE);
                binding.loadingViewMain.stopAnim();
                binding.networkErrViewMain.setVisibility(View.GONE);
                updateTopItemNextFocus(false);
                int delayMillis = mArrayObjectAdapter.size() > 0 ? 120 : 0;
                mArrayObjectAdapter.clear();
                mArrayObjectAdapter.addAll(0, mainViewModel.mainCategoryList);
                productCategoryAdapter.setDataList(mainViewModel.mainCategoryList);

                if(delayMillis > 0){
                    binding.horizontalGridViewMainCategory.postDelayed(() -> updateCategorySelected(), delayMillis);
                }else{
                    updateCategorySelected();
                }
            }else if(LoadState.Failed == loadState){
                if(mainViewModel.tryReqMainCategoryTimes > 0) {
                    mainViewModel.reqMainCategoryList();
                }else{
                    binding.loadingViewMain.setVisibility(View.GONE);
                    binding.loadingViewMain.stopAnim();
                    binding.networkErrViewMain.setVisibility(View.VISIBLE);
                    binding.networkErrViewMain.requestFocus();
                    updateTopItemNextFocus(true);
                }
            }else {
                binding.loadingViewMain.setVisibility(View.VISIBLE);
                binding.loadingViewMain.startAnim();
                binding.loadingViewMain.requestFocus();
                binding.networkErrViewMain.setVisibility(View.GONE);
                binding.horizontalGridViewMainCategory.setVisibility(View.GONE);
                binding.viewPagerProduct.setVisibility(View.GONE);
            }
        });

        mainViewModel.mManagerAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if(mainViewModel.managerAppUpgradeResp != null){
                    if(downloadBinder != null) {
                        handleManagerAppUpgrade(mainViewModel.managerAppUpgradeResp);
                    }
                }
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }
        });

        mainViewModel.mHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                UpgradeResp upgradeResp = mainViewModel.hostAppUpgradeResp;
                if (upgradeResp != null) {
                    showUpgradeDialog(upgradeResp);
                }
            }
        });
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
        careBroadcastReceiver = new CareBroadcastReceiver();
        registerReceiver(careBroadcastReceiver, intentFilter);
    }

    private void updateTopItemNextFocus(boolean networkErr){
        if(networkErr) {
            binding.itsvSettings.setNextFocusDownId(R.id.networkErrView_main);
            binding.itsvWifi.setNextFocusDownId(R.id.networkErrView_main);
            binding.itsvSearch.setNextFocusDownId(R.id.networkErrView_main);
            binding.itsvUser.setNextFocusDownId(R.id.networkErrView_main);
            binding.itsvNotify.setNextFocusDownId(R.id.networkErrView_main);
        }else{
            binding.itsvSettings.setNextFocusDownId(R.id.horizontalGridView_main_category);
            binding.itsvWifi.setNextFocusDownId(R.id.horizontalGridView_main_category);
            binding.itsvSearch.setNextFocusDownId(R.id.horizontalGridView_main_category);
            binding.itsvUser.setNextFocusDownId(R.id.horizontalGridView_main_category);
            binding.itsvNotify.setNextFocusDownId(R.id.horizontalGridView_main_category);
            binding.horizontalGridViewMainCategory.setVisibility(View.VISIBLE);
            binding.viewPagerProduct.setVisibility(View.VISIBLE);
            binding.networkErrViewMain.setFocusable(false);
            binding.idGroup.setReferencedIds(new int[]{
                    R.id.itsv_search,
                    R.id.itsv_user,
                    R.id.itsv_settings,
                    R.id.itsv_notify,
                    R.id.itsv_wifi,
                    R.id.tp_time,
                    R.id.iv_app_logo,
                    R.id.horizontalGridView_main_category,
            });
            binding.horizontalGridViewMainCategory.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        CommonVariableCacheUtils.getInstance().initOptions(getResources());
        CommonVariableCacheUtils.getInstance().initDrawable(getResources());
        mainViewModel.checkCrashLog();
        getFromSettingPage();
        checkUserState();
    }

    //点击设置界面账户跳转我的界面
    private void getFromSettingPage() {
        int currentItem = getIntent().getIntExtra("position", -1);
        if ((currentItem != -1) && (!isFromSetting)) {
            isFromSetting = true;
            ProdConstants.DEFAULT_FOCUS_INDEX = 0;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkUserState();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void checkUserState() {
        if (CommonUtils.isUserLogin()) {
            String account = CommonUtils.getUserInfo();
            binding.itsvUser.setIconText(account);
            binding.itsvUser.setImageBackground(getDrawable(R.drawable.selector_top_user_login));
        } else {
            binding.itsvUser.setIconText(getString(R.string.user));
            binding.itsvUser.setImageBackground(getDrawable(R.drawable.selector_top_user));
        }
    }

    @Override
    protected void onDestroy() {
        binding.horizontalGridViewMainCategory.removeOnChildViewHolderSelectedListener(onChildViewHolderSelectedListener);
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
        unregisterReceiver(careBroadcastReceiver);

        if ((downloadBinder != null) && (serviceConnection != null)) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            unbindService(serviceConnection);

        }
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }

        super.onDestroy();
    }

    private final OnChildViewHolderSelectedListener onChildViewHolderSelectedListener = new OnChildViewHolderSelectedListener() {
        @Override
        public void onChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder child, int position, int subPosition) {
            super.onChildViewHolderSelected(parent, child, position, subPosition);
            if (child != null & position != mCurrentPageIndex) {
                TextView currentTitle = child.itemView.findViewById(R.id.tv_main_title);
                if (isSkipTabFromViewPager) {
                    if (mOldTitle != null) {
                        mOldTitle.setTextColor(getResources().getColor(R.color.colorWhite));
                        Paint paint = mOldTitle.getPaint();
                        if (paint != null) {
                            paint.setFakeBoldText(false);
                            //viewpager切页标题不刷新，调用invalidate刷新
                            mOldTitle.invalidate();
                        }
                    }
                    currentTitle.setTextColor(getResources().getColor(R.color.colorBlue));
                    Paint paint = currentTitle.getPaint();
                    if (paint != null) {
                        paint.setFakeBoldText(true);
                        //viewpager切页标题不刷新，调用invalidate刷新
                        currentTitle.invalidate();
                    }
                }
                mOldTitle = currentTitle;
            }

            isSkipTabFromViewPager = false;
            setCurrentItemPosition(position);
            //恢复原来默认值
            if (isFromSetting) {
                isFromSetting = false;
                ProdConstants.DEFAULT_FOCUS_INDEX = 1;
            }
        }
    };

    private void setCurrentItemPosition(int position) {
        if (position != mCurrentPageIndex) {
            mCurrentPageIndex = position;
            binding.viewPagerProduct.setCurrentItem(position);
            SPUtils.getInstance().put(SharePrefer.selectFragment, ""+position);
        }

    }

    private void handleTitleVisible(boolean isShow) {
        if (isShow) {
            if (binding.idGroup.getVisibility() != View.VISIBLE) {
                binding.idGroup.setVisibility(View.VISIBLE);
            }
        } else {
            if (binding.idGroup.getVisibility() != View.GONE) {
                binding.idGroup.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onTopGroupInteraction(TopGroupAction topGroupAction) {
        if (topGroupAction == TopGroupAction.Hide) {
            handleTitleVisible(false);
        } else if (topGroupAction == TopGroupAction.Show) {
            handleTitleVisible(true);
        }
    }

    class CareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BaseConstants.PACKAGE_INSTALLED_ACTION.equals(intent.getAction())){
                Bundle extras = intent.getExtras();
                int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                String pluginName = extras.getString(BaseConstants.EXTRA_PLUGIN_NAME);
                if(LoadingPluginActivity.lastUnzipDonePlugin != null){
                    if(LoadingPluginActivity.lastUnzipDonePlugin.equals(pluginName)){
                        LoadingPluginActivity.lastUnzipDonePlugin = null;
                    }
                }
                Log.i(TAG, "handleCommonApkInstall() status=" + status + ", pluginName=" + pluginName);
                String pluginFilePath = extras.getString(BaseConstants.EXTRA_PLUGIN_FILE_PATH);
                if(PackageInstaller.STATUS_SUCCESS == status){
                    File file = new File(pluginFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    installingFileId = null;
                    handleCommonApkInstall();
                }else{
                    installingFileId = null;
                    handleCommonApkInstall();
                }
                /*if(installingMap.size() == 0){
                    SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, "");
                }*/
            }else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                updateWifiInfo();
                dealNetChange(context);
            }
        }
    }

    private void dealNetChange(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork && activeNetwork.isConnected()) {
            if (mainViewModel.hostAppUpgradeResp != null && downloadBinder != null){
                DownloadInfo  dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
                if ((dbDownloadInfo!=null) && (dbDownloadInfo.status != DownloadInfo.STATUS_SUCCESS)) {
                    downloadBinder.startDownload(dbDownloadInfo.fileId);
                }
            }

            if(mainViewModel.mldMainCategoryListLoadState.getValue() == null){
                checkShouldInitData();
            }else if(isLoadCache || LoadState.Failed ==  mainViewModel.mldMainCategoryListLoadState.getValue()){
                isLoadCache = false;
                mainViewModel.tryReqMainCategoryTimes = 3;
                mainViewModel.reqMainCategoryList();
            }else{
                mainViewModel.mldNetConnected.setValue(true);
            }

            if(null == mainViewModel.mManagerAppUpgradeState.getValue() || LoadState.Failed == mainViewModel.mManagerAppUpgradeState.getValue()){
                mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
            }else if(LoadState.Failed == mainViewModel.mHostAppUpgradeState.getValue()){
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateWifiInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            binding.itsvWifi.setImageBackground(getDrawable(R.drawable.selector_top_wifi));
            String wifiName = getConnectWifiSsid();
            if (wifiName != null && !wifiName.isEmpty()) {
                binding.itsvWifi.setIconText(wifiName);
            } else {
                binding.itsvWifi.setIconText("已连接");
            }
        } else {
            binding.itsvWifi.setImageBackground(getDrawable(R.drawable.selector_top_wifi_err));
            binding.itsvWifi.setIconText("WiFi");
        }
    }

    private String getConnectWifiSsid() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            String wifiSSID = wifiInfo.getSSID();
            return wifiSSID.replaceAll("\"", "");
        }
        return null;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (allPermissionsGranted()) {

            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                updateWifiInfo();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //首页检测版本更新只有一次，如果是VersionUpdateActivity，用户可以点击再取消再点击取消重复多次检测
    private void showUpgradeDialog(final UpgradeResp upgradeResp) {
        CustomUpgradeDialog customNormalDialog=new CustomUpgradeDialog(MainActivity.this, R.layout.layout_dialog_update);
        customNormalDialog.show();
        customNormalDialog.title.setText("检测到新版本，快来体验吧！");
        customNormalDialog.positive.setText("升级");
        customNormalDialog.cancel.setText("取消");
        customNormalDialog.forceConfirm.setText("升级");
        if (upgradeResp.isForcible()) {
            customNormalDialog.forceConfirm.setVisibility(View.VISIBLE);
            customNormalDialog.forceConfirm.requestFocus();
        } else {
            customNormalDialog.forceConfirm.setVisibility(View.GONE);
            customNormalDialog.positive.requestFocus();
        }
        customNormalDialog.setOnClickListener(new CustomUpgradeDialog.OnClickListener() {
            @Override
            public void onConFirm() {
                dealUpgrade(upgradeResp);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void forceUpgrade() {
                dealUpgrade(upgradeResp);
            }

            @Override
            public void forceExit() {

            }
        });

    }

    private void dealUpgrade(final UpgradeResp upgradeResp) {
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
        if(downloadInfo != null){
            if(downloadInfo.version.equals(upgradeResp.getSoftwareVersion())){//mean already add
                if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()){
                        handleHostInstall(downloadInfo.filePath, downloadInfo.fileId);
                    }else{//something wrong?
                        downloadBinder.startDownload(downloadInfo);
                        progressDialog.show();
                    }
                }else if(downloadInfo.status == DownloadInfo.STATUS_STOPPED){
                    downloadBinder.startDownload(downloadInfo);
                    progressDialog.show();
                }else{
                    progressDialog.show();
                }
            }else{//old version in db
                downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
                progressDialog.show();
            }
        }else{
            downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
            progressDialog.show();
        }
    }

    private boolean handleManagerAppUpgrade(final UpgradeResp upgradeResp){
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.MANAGER_APP_SOFTWARE_CODE);
        if(downloadInfo != null){
            if(downloadInfo.version.equals(upgradeResp.getSoftwareVersion())){//mean already add
                if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()){
                        if(!isInQueue(downloadInfo.fileId)) {
                            addToQueueNextCheckInstall(downloadInfo.fileId);
                        }
                    }else{//something wrong? the file removed or same version update?
                        CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                        return downloadBinder.startDownload(downloadInfo);
                    }
                }else {
                    return downloadBinder.startDownload(downloadInfo);
                }
            }else{//old version in db
                return downloadBinder.startDownload(DownloadHelper.buildManagerUpgradeDownloadInfo(this, upgradeResp));
            }
        }else{
            return downloadBinder.startDownload(DownloadHelper.buildManagerUpgradeDownloadInfo(this, upgradeResp));
        }

        return true;
    }
}