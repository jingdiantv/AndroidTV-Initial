package com.zwn.user.ui;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.ReqState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.protocol.request.AkSkReq;
import com.zwn.user.data.protocol.request.MsgCodeReq;
import com.zwn.user.data.protocol.request.MsgLoginReq;
import com.zwn.user.data.protocol.request.PwdLoginReq;
import com.zwn.user.data.protocol.response.AkSkResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.protocol.response.MsgCodeResp;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginCenterViewModel extends BaseViewModel {

    private final UserRepository mUserRepository;
    private final Context mContext;
    public static final String FINISH_COUNT_DOWN_TAG = "###COUNT_DOWN_END###";

    public MutableLiveData<ReqState> pReqSmsCodeState = new MutableLiveData<>();
    public MutableLiveData<ReqState> pSmsLoginState = new MutableLiveData<>();
    public MutableLiveData<ReqState> pAcctLoginState = new MutableLiveData<>();
    public MutableLiveData<String> pCountDownTime = new MutableLiveData<>();

    private final int TOTAL_TIME = 60;
    private final int ONCE_TIME = 1;

    public String pSmsUUID;

    public LoginCenterViewModel(UserRepository userRepository, Context context) {
        mUserRepository = userRepository;
        mContext = context;
    }

    public String getString(String key) {
        return mUserRepository.getString(key);
    }

    public void removeUserInfo() {
        CommonUtils.logoutClear();
    }

    //----------------------------倒计时 START----------------------------
    public void startCountDown() {
        countDownTimer.start();
    }

    private final CountDownTimer countDownTimer = new CountDownTimer(
            TOTAL_TIME * 1000,
            ONCE_TIME * 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            String value = "重新获取" + (int) (millisUntilFinished / 1000) + "S";
            pCountDownTime.setValue(value);
        }

        @Override
        public void onFinish() {
            pCountDownTime.setValue(FINISH_COUNT_DOWN_TAG);
        }
    };

    public void cancelCountDown() {
        countDownTimer.cancel();
    }
    //----------------------------倒计时 END----------------------------

    /**
     * 请求短信验证码
     * @param phoneNumber 电话号码
     */
    public void reqSmsCode(String phoneNumber) {
        MsgCodeReq msgCodeReq = new MsgCodeReq("0", phoneNumber);
        mUserRepository.getMsgCode(msgCodeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<MsgCodeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<MsgCodeResp> response) {
                        if (response.data != null) {
                            pSmsUUID = response.data.uuid;
                            pReqSmsCodeState.setValue(ReqState.SUCCESS);
                        } else {
                            Toast.makeText(mContext, response.message, Toast.LENGTH_SHORT).show();
                            pReqSmsCodeState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(mContext, "获取验证码失败，请检查网络状态", Toast.LENGTH_SHORT).show();
                        pReqSmsCodeState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 短信验证码登录
     * @param phoneNumber 电话号码
     * @param code 短信验证码
     */
    public void reqSmsLogin(String phoneNumber, String code) {
        MsgLoginReq msgLoginReq = new MsgLoginReq("1", phoneNumber, code, pSmsUUID);
        mUserRepository.msgLogin(msgLoginReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> response) {
                        if (response.data != null) {
                            String userToken = response.data.token;
                            mUserRepository.putValue(SharePrefer.userToken, userToken);
                            mUserRepository.putValue(SharePrefer.userAccount, msgLoginReq.telephone);
                            CommonVariableCacheUtils.getInstance().token = userToken;
                            reqAkSkInfo(LoginWay.LOGIN_SMS);
                        } else {
                            Toast.makeText(mContext, response.message, Toast.LENGTH_SHORT).show();
                            pSmsLoginState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(mContext, "登录失败，请检查网络状态", Toast.LENGTH_SHORT).show();
                        pSmsLoginState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 账号密码登录
     * @param account 账号
     * @param password 密码
     */
    public void reqAcctLogin(String account, String password) {
        PwdLoginReq pwdLoginReq = new PwdLoginReq(
                "0",
                account,
                password
        );
        mUserRepository.pwdLogin(pwdLoginReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> response) {
                        if (response.data != null) {
                            String userToken = response.data.token;
                            mUserRepository.putValue(SharePrefer.userToken, userToken);
                            mUserRepository.putValue(SharePrefer.userAccount, pwdLoginReq.loginName);
                            CommonVariableCacheUtils.getInstance().token = userToken;
                            reqAkSkInfo(LoginWay.LOGIN_ACCT);
                        } else {
                            Toast.makeText(mContext, response.message, Toast.LENGTH_SHORT).show();
                            pAcctLoginState.setValue(ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(mContext, "登录失败，请检查网络状态", Toast.LENGTH_SHORT).show();
                        pAcctLoginState.setValue(ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 获取AK SK
     * @param loginWay
     */
    public void reqAkSkInfo(LoginWay loginWay) {
        AkSkReq akSkReq = new AkSkReq(BaseConstants.AUTH_SYSTEM_CODE);
        mUserRepository.getAkSkInfo(akSkReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<AkSkResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<AkSkResp> response) {
                        AkSkResp akSkResp = response.data;
                        if (akSkResp != null && akSkResp.akCode != null && !akSkResp.akCode.isEmpty()) {
                            Gson gson = new Gson();
                            String akSkString = gson.toJson(akSkResp);
                            mUserRepository.putValue(SharePrefer.akSkInfo, akSkString);
                            respAkSkInfo(loginWay, ReqState.SUCCESS);
                        } else {
                            Toast.makeText(mContext, response.message, Toast.LENGTH_SHORT).show();
                            respAkSkInfo(loginWay, ReqState.FAILED);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mUserRepository.putValue(SharePrefer.userToken, "");
                        mUserRepository.putValue(SharePrefer.userAccount, "");
                        CommonVariableCacheUtils.getInstance().token = "";
                        Toast.makeText(mContext, "登录失败，请检查网络状态", Toast.LENGTH_SHORT).show();
                        respAkSkInfo(loginWay, ReqState.ERROR);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void respAkSkInfo(LoginWay loginWay, ReqState reqState) {
        switch (loginWay) {
            case LOGIN_SMS:
                pSmsLoginState.setValue(reqState);
                break;
            case LOGIN_ACCT:
                pAcctLoginState.setValue(reqState);
                break;
            default:
                break;
        }
    }

    enum LoginWay {
        LOGIN_ACCT,
        LOGIN_SMS
    }
}


