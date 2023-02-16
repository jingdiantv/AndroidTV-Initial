package com.zwn.user.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.zee.manager.IZeeCallback;
import com.zee.manager.IZeeManager;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zwn.user.R;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.databinding.ActivityUserCenterBinding;
import com.zwn.user.ui.mine.AboutUsFragment;
import com.zwn.user.ui.mine.DownloadsFragment;
import com.zwn.user.ui.mine.FavoritesFragment;
import com.zwn.user.ui.mine.HistoryFragment;
import com.zwn.user.ui.mine.MessageCenterFragment;

public class UserCenterActivity extends BaseActivity {

    private static final String TAG = "UserCenterActivity";

    private ActivityUserCenterBinding mBinding;
    private FavoritesFragment mFavoritesFragment;
    private AboutUsFragment mAboutUsFragment;
    private MessageCenterFragment mMessageCenterFragment;
    private HistoryFragment mHistoryFragment;
    private DownloadsFragment mDownloadsFragment;

    private String mUserFunc;
    private boolean mLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_center);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        mUserFunc = intent.getStringExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC);
        switch (mUserFunc) {
            case UserCenterConf.FUNC_USER_COLLECT:
                mFavoritesFragment = FavoritesFragment.newInstance();
                replaceFragment(mFavoritesFragment);
                break;
            case UserCenterConf.FRAGMENT_ABOUT_US:
                mAboutUsFragment = AboutUsFragment.newInstance();
                replaceFragment(mAboutUsFragment);
                break;
            case UserCenterConf.FRAGMENT_MESSAGE_CENTER:
                mMessageCenterFragment = MessageCenterFragment.newInstance();
                replaceFragment(mMessageCenterFragment);
                break;
            case UserCenterConf.FUNC_USER_HISTORY:
                mHistoryFragment = HistoryFragment.newInstance();
                replaceFragment(mHistoryFragment);
                break;
            case UserCenterConf.FUNC_USER_DOWNLOAD:
                mDownloadsFragment = DownloadsFragment.newInstance();
                replaceFragment(mDownloadsFragment);
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_user_center, fragment)
                .commit();
    }

    private void showDelButton() {
        switch (mUserFunc) {
            case UserCenterConf.FUNC_USER_COLLECT:
                if (mFavoritesFragment.getFavoritesNum() > 0) {
                    mFavoritesFragment.showDelButton();
                }
                break;
            case UserCenterConf.FRAGMENT_MESSAGE_CENTER:
                if (mMessageCenterFragment.getMsgNum() > 0) {
                    mMessageCenterFragment.changeDelButtonState();
                }
                break;
            case UserCenterConf.FUNC_USER_HISTORY:
                if (mHistoryFragment.getHistNum() > 0) {
                    mHistoryFragment.isOnDelete();
                }
                break;
            case UserCenterConf.FUNC_USER_DOWNLOAD:
                if (mDownloadsFragment.getItemSize() > 0) {
                    mDownloadsFragment.isOnDelete();
                }
                break;
        }
    }

    public void isLoading(boolean loading) {
        this.mLoading = loading;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mLoading) return true;
                pressUp();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mLoading) return true;
                pressDown();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (mLoading) return true;
                pressLeft();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mLoading) return true;
                pressRight();
                break;
            case KeyEvent.KEYCODE_MENU:
                if (mLoading) return true;
                showDelButton();
                break;
            case KeyEvent.KEYCODE_ENTER:
                if (mLoading) return true;
                break;
            case KeyEvent.KEYCODE_F5:
                finish();
                break;
            default:
                Log.d(TAG, "点击了按键：" + keyCode);
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void pressRight() {
        if (mUserFunc.equals(UserCenterConf.FRAGMENT_MESSAGE_CENTER)) {
            if (mMessageCenterFragment.getMsgNum() > 0) {
                mMessageCenterFragment.moveRight();
            }
        }
    }

    private void pressLeft() {
        if (mUserFunc.equals(UserCenterConf.FRAGMENT_MESSAGE_CENTER)) {
            if (mMessageCenterFragment.getMsgNum() > 0) {
                mMessageCenterFragment.moveLeft();
            }
        }
    }

    private void pressUp() {
        if (mUserFunc.equals(UserCenterConf.FRAGMENT_MESSAGE_CENTER)) {
            if (mMessageCenterFragment.getMsgNum() > 0) {
                mMessageCenterFragment.moveUp();
            }
        }
    }

    private void pressDown() {
        if (mUserFunc.equals(UserCenterConf.FRAGMENT_MESSAGE_CENTER)) {
            if (mMessageCenterFragment.getMsgNum() > 0) {
                mMessageCenterFragment.moveDown();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (zeeManager != null) {
            try {
                zeeManager.removeZeeCallback(iZeeCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }

    private IZeeManager zeeManager = null;
    public void bindManagerService() {
        Intent bindIntent = new Intent(BaseConstants.MANAGER_SERVICE_ACTION);
        bindIntent.setPackage(BaseConstants.MANAGER_PACKAGE_NAME);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void unbindManagerService() {
        unbindService(serviceConnection);
    }

    private static final IZeeCallback iZeeCallback = new IZeeCallback.Stub() {
        @Override
        public void onDeletePackage(boolean success, String pkgName) throws RemoteException {
            Log.i(TAG, "onDeletePackage() success=" + success + ", pkgName=" + pkgName);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()");
            zeeManager = IZeeManager.Stub.asInterface(service);
            if(zeeManager != null){
                try {
                    zeeManager.addZeeCallback(iZeeCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()");
        }
    };

    public void remoteDeleteCall(String packageName){
        if(zeeManager != null){
            try {
                zeeManager.deletePackage(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
