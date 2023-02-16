package com.zwn.launcher;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.zeewain.base.BaseApplication;
import com.zwn.lib_download.db.CareController;

public class ZeeApplication extends BaseApplication {

    private HttpProxyCacheServer proxy;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        CareController.init(this);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        ZeeApplication app = (ZeeApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }
}
