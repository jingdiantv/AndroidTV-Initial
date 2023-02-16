package com.zeewain.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DiskCacheManager;
import com.zeewain.base.utils.SPUtils;

public class BaseApplication extends Application {
    public static Context applicationContext;
    public static String platformInfo = null;

    @Override
    public void onCreate() {
        super.onCreate();
        DiskCacheManager.init(this);
        platformInfo = buildPlatformInfo();
    }

    //AndroidTVAIIP/1.0.000 (ZWN_AIIP_001 1.0; Android 9.***)
    public String buildPlatformInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("AndroidTVAIIP/")
                .append(ApkUtil.getAppVersionName(getApplicationContext()))
                .append(" (ZWN_AIIP_001 1.0; Android ")
                .append(Build.VERSION.RELEASE)
                .append(")");
        return stringBuffer.toString();
    }

    public static synchronized void handleUnauthorized(){
        try {
            String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
            if(userToken != null && !userToken.isEmpty()) {
                CommonUtils.logoutClear();
                Intent intent = new Intent(applicationContext, Class.forName("com.zwn.user.ui.LoginCenterActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                applicationContext.startActivity(intent);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
