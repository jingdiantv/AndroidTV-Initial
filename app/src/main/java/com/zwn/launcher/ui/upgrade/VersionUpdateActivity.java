package com.zwn.launcher.ui.upgrade;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.zeewain.base.Views.CustomProgressDialog;
import com.zeewain.base.Views.CustomUpgradeDialog;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.NetworkUtil;
import com.zwn.launcher.R;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.protocol.request.UpgradeReq;
import com.zwn.launcher.data.protocol.response.UpgradeResp;
import com.zwn.launcher.ui.detail.ProDetailViewModel;
import com.zwn.launcher.ui.detail.ProDetailViewModelFactory;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;

public class VersionUpdateActivity extends BaseActivity {
    private final static String TAG = "VersionUpdateActivity";

    private View mVersionView;

    private ProDetailViewModel detailViewModel;
    private DownloadService.DownloadBinder downloadBinder = null;
    private CustomProgressDialog progressDialog;
    private CustomUpgradeDialog customUpgradeDialog;

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
        public void onSuccess(String fileId, int type, File file) {}

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId .equals(BaseConstants.HOST_APP_SOFTWARE_CODE) ) {
                runOnUiThread(() -> {
                    progressDialog.setTitle("网络异常");
                });
            }
        }

        @Override
        public void onPaused(String fileId) {

        }

        @Override
        public void onCancelled(String fileId) {

        }

        @Override
        public void onUpdate(String fileId) {

        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            if (downloadBinder != null) {
                downloadBinder.registerDownloadListener(downloadListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_update);

        bindService();
        initProgressDialog();
        initView();
        initListener();
        initObserver();
    }

    private void initView() {
        detailViewModel = ViewModelProviders.of(this, new ProDetailViewModelFactory(DataRepository.getInstance())).get(ProDetailViewModel.class);
        String versionName = ApkUtil.getAppVersionName(this);

        mVersionView = (View) findViewById(R.id.include_update_version);
        ((TextView) mVersionView.findViewById(R.id.tv_set_item_title)).setText("目前版本");
        ((TextView) mVersionView.findViewById(R.id.tv_set_item_cont)).setText(versionName);
    }

    private void initListener() {
        mVersionView.setOnClickListener(v -> checkAppUpdate());
    }

    private void initObserver() {
        detailViewModel.mUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                UpgradeResp upgradeResp = detailViewModel.upgradeResp;
                if (upgradeResp != null) {
                    showUpgradeDialog(upgradeResp);
                } else {
                    showToast("已是最新版本！");
                }
            }
        });
    }

    private void initProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(VersionUpdateActivity.this);
            progressDialog.setTitle("正在更新");
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
    }

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent bindIntent = new Intent(this.getApplicationContext(), DownloadService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void showUpgradeDialog(final UpgradeResp upgradeResp) {
        if (customUpgradeDialog ==null){
            customUpgradeDialog = new CustomUpgradeDialog(VersionUpdateActivity.this, R.layout.layout_dialog_update);
            customUpgradeDialog.show();
            customUpgradeDialog.title.setText("检测到新版本，快来体验吧！");
            customUpgradeDialog.positive.setText("升级");
            customUpgradeDialog.cancel.setText("取消");
            customUpgradeDialog.forceConfirm.setText("升级");
            if (upgradeResp.isForcible()) {
                customUpgradeDialog.forceConfirm.setVisibility(View.VISIBLE);
                customUpgradeDialog.forceConfirm.requestFocus();
            } else {
                customUpgradeDialog.forceConfirm.setVisibility(View.GONE);
                customUpgradeDialog.positive.requestFocus();
            }
            customUpgradeDialog.setOnClickListener(new CustomUpgradeDialog.OnClickListener() {
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
        } else {
            if (!customUpgradeDialog.isShowing()){
                customUpgradeDialog.show();
            }
        }
    }

    private void checkAppUpdate() {
        String version = ApkUtil.getAppVersionName(this);
        if ((version != null) ) {
            detailViewModel.getUpgradeVersionInfo(new UpgradeReq(version, BaseConstants.HOST_APP_SOFTWARE_CODE));
        }
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

    private void handleHostInstall(String hostApkPath, String fileId){
        Intent intent = new Intent(BaseConstants.PACKAGE_INSTALLED_ACTION);
        ComponentName componentName = new ComponentName(BaseConstants.MANAGER_PACKAGE_NAME, BaseConstants.MANAGER_INSTALL_ACTIVITY);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BaseConstants.EXTRA_APK_PATH, hostApkPath);
        intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, fileId);
        startActivity(intent);
    }

    @Override
    public void dealNetWorkChange(Context context) {
        boolean networkAvailable = NetworkUtil.isNetworkAvailable(this);
        if (networkAvailable){
            DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
            if ((dbDownloadInfo!=null) && (dbDownloadInfo.status != DownloadInfo.STATUS_SUCCESS)) {
                if(downloadBinder != null)
                    downloadBinder.startDownload(dbDownloadInfo.fileId);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if ((downloadBinder != null) && (serviceConnection != null)) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            unbindService(serviceConnection);
        }

        if (customUpgradeDialog != null) {
            if (customUpgradeDialog.isShowing()) {
                customUpgradeDialog.cancel();
            }
            customUpgradeDialog = null;
        }

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
        super.onDestroy();
    }
}
