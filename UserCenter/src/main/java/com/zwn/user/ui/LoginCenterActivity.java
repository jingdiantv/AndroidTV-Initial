package com.zwn.user.ui;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.ui.login.SelectLoginWaysFragment;
import com.zwn.user.ui.login.SignOutFragment;

public class LoginCenterActivity extends BaseActivity {

    private final String TAG = "LoginCenterActivity";

    private SelectLoginWaysFragment mSelectLoginWaysFragment;
    private Fragment mSignOutFragment;

    private boolean mLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_login_center);

        initView();
    }

    private void initView() {
        LoginCenterViewModel viewModel = new ViewModelProvider(this, new LoginCenterViewModelFactory(
                UserRepository.getInstance(), this)).get(LoginCenterViewModel.class);
        String token = viewModel.getString(SharePrefer.userToken);
        if (token.isEmpty()) {
            mSelectLoginWaysFragment = SelectLoginWaysFragment.newInstance();
            replaceFragment(mSelectLoginWaysFragment, false, false);
        } else {
            String userAccount = viewModel.getString(SharePrefer.userAccount);
            mSignOutFragment = SignOutFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(UserCenterConf.USER_ACCOUNT_KEY, userAccount);
            mSignOutFragment.setArguments(bundle);

            replaceFragment(mSignOutFragment, false, true);
        }
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, boolean clearStack) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (clearStack) {
            if (mSelectLoginWaysFragment != null) {
                transaction.hide(mSelectLoginWaysFragment);
            }
            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStackImmediate();
            }
            if (mSignOutFragment == null) {
                transaction.add(R.id.fragment_login_center, fragment);
                mSignOutFragment = fragment;
            } else {
                if (mSelectLoginWaysFragment == null) {
                    transaction.replace(R.id.fragment_login_center, fragment);
                } else {
                    mSignOutFragment.setArguments(fragment.getArguments());
                    transaction.show(mSignOutFragment);
                }
            }
        } else {
            transaction.replace(R.id.fragment_login_center, fragment);
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void showSelectFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.hide(mSignOutFragment);
        if (mSelectLoginWaysFragment == null) {
            mSelectLoginWaysFragment = SelectLoginWaysFragment.newInstance();
            transaction.add(R.id.fragment_login_center, mSelectLoginWaysFragment);
        } else {
            transaction.show(mSelectLoginWaysFragment);
        }

        transaction.commit();
    }

    public void isLoading(boolean loading) {
        this.mLoading = loading;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_ENTER:
                if (mLoading) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_F5:
                finish();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}