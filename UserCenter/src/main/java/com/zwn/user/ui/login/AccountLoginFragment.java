package com.zwn.user.ui.login;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zeewain.base.model.ReqState;
import com.zwn.user.BuildConfig;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.data.protocol.request.PwdLoginReq;
import com.zwn.user.databinding.LoginAcctFragmentBinding;
import com.zwn.user.ui.LoginCenterActivity;
import com.zwn.user.ui.LoginCenterViewModel;
import com.zwn.user.ui.LoginCenterViewModelFactory;
import com.zwn.user.utils.KeyBoardChangeUtil;

import java.util.Objects;

public class AccountLoginFragment extends Fragment {

    private final static String TAG = "AccountLoginFragment";

    private Activity mActivity;
    private LoginAcctFragmentBinding mBinding;
    private LoginCenterViewModel mViewModel;

    private SignOutFragment mSignOutFragment;
    private InputMethodManager mImm;
    private Animation mAnimation;

    private String mUserAccount;
    private boolean mShowPassword = false;

    public static AccountLoginFragment newInstance() {
        return new AccountLoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = LoginAcctFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initViewListener();
        initObserve();
    }

    private void initData() {
        mActivity = Objects.requireNonNull(getActivity());
        mSignOutFragment = SignOutFragment.newInstance();
        mBinding.etLoginAcctAcct.requestFocus();
        mViewModel = new ViewModelProvider(this, new LoginCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(LoginCenterViewModel.class);
        mImm =  (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mBinding.ivLoginAcctLoading.setImageResource(R.mipmap.loading);
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
        mAnimation.setInterpolator(new LinearInterpolator());
        if (BuildConfig.DEBUG) {
            mBinding.etLoginAcctAcct.setText("18100730441");
            mBinding.etLoginAcctPwd.setText("w123456");
            mBinding.btnLoginAcctLogin.requestFocus();
        }
    }

    private void initViewListener() {
        mBinding.btnLoginAcctLogin.setOnClickListener(v -> {
            mUserAccount = mBinding.etLoginAcctAcct.getText().toString();
            String password = mBinding.etLoginAcctPwd.getText().toString();
            if (mUserAccount.length() < 3) {
                Toast.makeText(getActivity(), "账号错误", Toast.LENGTH_SHORT).show();
                return;
            }
            mBinding.ivLoginAcctLoading.setVisibility(View.VISIBLE);
            mBinding.ivLoginAcctLoading.startAnimation(mAnimation);
            ((LoginCenterActivity) mActivity).isLoading(true);
            mBinding.btnLoginAcctLogin.setClickable(false);
            mViewModel.reqAcctLogin(mUserAccount, password);
        });

        mBinding.clLoginAcctCtrlPwd.setOnClickListener(v -> {
            int drawableId = R.drawable.open_eyes_selector;
            if (mShowPassword) {
                drawableId = R.drawable.close_eyes_selector;
                mBinding.tvLoginAcctCtrlPwd.setText("显示");
                mBinding.etLoginAcctPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                mBinding.tvLoginAcctCtrlPwd.setText("隐藏");
                mBinding.etLoginAcctPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            mBinding.ivLoginAcctCtrlPwd.setImageResource(drawableId);
            mShowPassword = !mShowPassword;
        });

        KeyBoardChangeUtil.getInstance().addOnKeyBoardChangeListener(mActivity, (visible, windowBottom) -> {
            ScrollView.LayoutParams layoutParams = (ScrollView.LayoutParams) mBinding.svLoginAcct.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, windowBottom);
            mBinding.svLoginAcct.setLayoutParams(layoutParams);
            if (visible) {
                mBinding.svLoginAcct.post(() -> {
                    ObjectAnimator objectAnimator = ObjectAnimator
                            .ofInt(mBinding.svLoginAcct, "scrollY", 0, mBinding.btnLoginAcctLogin.getBottom() + windowBottom)
                            .setDuration(800);
                    objectAnimator.start();
                });
            }
        });

        mBinding.etLoginAcctPwd.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if(mImm != null) {
                    mImm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(), 0);
                }
            }
            return false;
        });
    }

    private void initObserve() {
        mViewModel.pAcctLoginState.observe(this, reqState -> {
            mBinding.ivLoginAcctLoading.clearAnimation();
            mBinding.ivLoginAcctLoading.setVisibility(View.INVISIBLE);
            ((LoginCenterActivity) mActivity).isLoading(false);
            mBinding.btnLoginAcctLogin.setClickable(true);
            if (reqState.equals(ReqState.SUCCESS)) {
                Bundle bundle = new Bundle();
                bundle.putString(UserCenterConf.USER_ACCOUNT_KEY, mUserAccount);
                mSignOutFragment.setArguments(bundle);
                if (mImm != null) {
                    mImm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(), 0);
                }
                ((LoginCenterActivity) mActivity).finish();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mSignOutFragment = null;
        mViewModel = null;
        KeyBoardChangeUtil.getInstance().removeOnKeyBoardChangeListener();
    }
}
