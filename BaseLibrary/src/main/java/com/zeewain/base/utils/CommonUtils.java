package com.zeewain.base.utils;

import android.content.Context;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;

import java.io.File;

public class CommonUtils {

    public static String getFileUsePath(String fileId, String version, int type, Context context){
        if(type == BaseConstants.DownloadFileType.HOST_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.MANAGER_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.PLUGIN_APP){
            return context.getExternalCacheDir().getPath() + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.SHARE_LIB){
            return context.getFilesDir().getPath() + "/" + fileId + "_" + version + ".zip";
        }else{
            return fileId;
        }
    }

    public static String getModelStorePath(String modelFileName, Context context){
        String path = context.getFilesDir().getPath() + "/models";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        return path + "/" + modelFileName;
    }

    public static boolean createOrClearPluginModelDir(){
        File file = new File(BaseConstants.PLUGIN_MODEL_PATH);
        if(!file.exists()){
            return file.mkdirs();
        }else {
            File[] files = file.listFiles();
            for(File tmpFile : files){
                if (tmpFile.isFile()){
                    tmpFile.delete();
                }
            }
        }
        return true;
    }

    public static boolean isUserLogin(){
        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
        if(userToken != null && !userToken.isEmpty()){
            return true;
        }
        return false;
    }

    public static String getUserInfo(){
        return SPUtils.getInstance().getString(SharePrefer.userAccount);
    }

    public static void logoutClear(){
        SPUtils.getInstance().remove(SharePrefer.userToken);
        SPUtils.getInstance().remove(SharePrefer.userAccount);
        SPUtils.getInstance().remove(SharePrefer.akSkInfo);
        CommonVariableCacheUtils.getInstance().token = "";
    }

}
