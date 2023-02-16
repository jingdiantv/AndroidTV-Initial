package com.zwn.user.utils;

import com.google.gson.Gson;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.utils.SPUtils;
import com.zwn.user.data.protocol.response.AkSkResp;

public class UserCenterUtil {
    public static AkSkResp getAkSkInfo(){
        String akSkInfoString = SPUtils.getInstance().getString(SharePrefer.akSkInfo);
        if(akSkInfoString != null && !akSkInfoString.isEmpty()){
            Gson gson = new Gson();
            AkSkResp akSkResp = gson.fromJson(akSkInfoString, AkSkResp.class);
            return akSkResp;
        }

        return null;
    }
}
