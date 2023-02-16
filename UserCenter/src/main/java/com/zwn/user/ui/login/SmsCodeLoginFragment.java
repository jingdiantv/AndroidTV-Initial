package com.zwn.user.ui.login;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
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
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.data.protocol.request.MsgCodeReq;
import com.zwn.user.data.protocol.request.MsgLoginReq;
import com.zwn.user.databinding.LoginSmsFragmentBinding;
import com.zwn.user.ui.LoginCenterActivity;
import com.zwn.user.ui.LoginCenterViewModel;
import com.zwn.user.ui.LoginCenterViewModelFactory;
import com.zwn.user.utils.AndroidHelper;
import com.zwn.user.utils.KeyBoardChangeUtil;

import java.util.Objects;

public class SmsCodeLoginFragment extends Fragment {

    private Activity mActivity;
    private LoginSmsFragmentBinding mBinding;
    private LoginCenterViewModel mViewModel;

    private SignOutFragment mSignOutFragment;
    private InputMethodManager mImm;
    private Animation mAnimation;

    private String mUserPhoneNumber;

    public static SmsCodeLoginFragment newInstance() {
        return new SmsCodeLoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = LoginSmsFragmentBinding.inflate(inflater, container, false);
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
        mSignOutFragment = SignOutFragment.newInstance();
        mBinding.etLoginSmsInput.requestFocus();
        mViewModel = new ViewModelProvider(this, new LoginCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(LoginCenterViewModel.class);
        mActivity = Objects.requireNonNull(getActivity());
        mImm =  (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mBinding.ivLoginSmsLoading.setImageResource(R.mipmap.loading);
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
        mAnimation.setInterpolator(new LinearInterpolator());
    }

    private void initViewListener() {
        mBinding.btnLoginSmsLogin.setOnClickListener(v -> {
            if (mBinding.btnLoginSmsLogin.getText().toString().equals("登录")) {
                String code = mBinding.etLoginSmsInput.getText().toString();
                if (!AndroidHelper.isNumeric(code)) {
                    Toast.makeText(mActivity, "验证码错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                forbidMove();
                mViewModel.reqSmsLogin(mUserPhoneNumber, code);
            } else {
                mUserPhoneNumber = mBinding.etLoginSmsInput.getText().toString();
                if (!AndroidHelper.isPhone(mUserPhoneNumber)) {
                    Toast.makeText(mActivity, "手机号错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                forbidMove();
                mViewModel.reqSmsCode(mUserPhoneNumber);
            }
        });

        mBinding.btnLoginSmsGetSms.setOnClickListener(v -> {
            mBinding.btnLoginSmsGetSms.setEnabled(false);
            mViewModel.reqSmsCode(mUserPhoneNumber);
        });

        KeyBoardChangeUtil.getInstance().addOnKeyBoardChangeListener(mActivity, (visible, windowBottom) -> {
            ScrollView.LayoutParams layoutParams = (ScrollView.LayoutParams) mBinding.svLoginSms.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, windowBottom);
            mBinding.svLoginSms.setLayoutParams(layoutParams);
            if (visible) {
                mBinding.svLoginSms.post(() -> {
                    ObjectAnimator objectAnimator = ObjectAnimator
                            .ofInt(mBinding.svLoginSms, "scrollY", 0, mBinding.svLoginSms.getBottom() + windowBottom)
                            .setDuration(800);
                    objectAnimator.start();
                });
            }
        });

        mBinding.etLoginSmsInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (mImm != null) {
                    mImm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(), 0);
                }
            }
            return false;
        });
    }

    private void initObserve() {
        mViewModel.pCountDownTime.observe(this, value -> {
            if (value.equals(LoginCenterViewModel.FINISH_COUNT_DOWN_TAG)) {
                mBinding.btnLoginSmsGetSms.setEnabled(true);
                mBinding.btnLoginSmsGetSms.setText("重新获取");
            } else {
                mBinding.btnLoginSmsGetSms.setText(value);
            }
        });

        mViewModel.pReqSmsCodeState.observe(this, reqState -> {
            recoverMove();
            if (reqState.equals(ReqState.SUCCESS)) {
                String showText = "验证码已发送到" + mUserPhoneNumber.substring(0, 3) + "****"
                        + mUserPhoneNumber.substring(7, 11) + "手机";
                mBinding.tvLoginSmsMsg.setText(showText);
                mBinding.btnLoginSmsLogin.setText("登录");

                mBinding.tvLoginSmsAreaCode.setVisibility(View.INVISIBLE);
                mBinding.btnLoginSmsGetSms.setVisibility(View.VISIBLE);

                SpannableString inputCodeHint = new SpannableString("输入短信验证码");
                mBinding.etLoginSmsInput.setText("");
                mBinding.etLoginSmsInput.setHint(inputCodeHint);
                mBinding.etLoginSmsInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});

                mViewModel.startCountDown();
                mBinding.etLoginSmsInput.requestFocus();
            }
        });

        mViewModel.pSmsLoginState.observe(this, reqState -> {
            recoverMove();
            if (reqState.equals(ReqState.SUCCESS)) {
                mViewModel.cancelCountDown();

                Bundle bundle = new Bundle();
                bundle.putString(UserCenterConf.USER_ACCOUNT_KEY, mUserPhoneNumber);
                mSignOutFragment.setArguments(bundle);
                if (mImm != null) {
                    mImm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(), 0);
                }
                ((LoginCenterActivity) mActivity).finish();
            }
        });
    }

    private void forbidMove() {
        mBinding.ivLoginSmsLoading.setVisibility(View.VISIBLE);
        mBinding.ivLoginSmsLoading.setAnimation(mAnimation);
        ((LoginCenterActivity) mActivity).isLoading(true);
        mBinding.btnLoginSmsLogin.setClickable(false);
    }

    private void recoverMove() {
        mBinding.btnLoginSmsLogin.setClickable(true);
        mBinding.ivLoginSmsLoading.clearAnimation();
        mBinding.ivLoginSmsLoading.setVisibility(View.INVISIBLE);
        ((LoginCenterActivity) mActivity).isLoading(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mSignOutFragment = null;
        mViewModel.cancelCountDown();
        mViewModel = null;
        KeyBoardChangeUtil.getInstance().removeOnKeyBoardChangeListener();
    }
}
