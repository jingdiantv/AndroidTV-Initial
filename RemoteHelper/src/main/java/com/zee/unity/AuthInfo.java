package com.zee.unity;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthInfo implements Parcelable {
    private String akCode;
    private String skCode;
    private String hostPkg;
    private String authUri;
    private String authToken;

    public AuthInfo(String akCode, String skCode, String hostPkg, String authUri, String authToken) {
        this.akCode = akCode;
        this.skCode = skCode;
        this.hostPkg = hostPkg;
        this.authUri = authUri;
        this.authToken = authToken;
    }

    protected AuthInfo(Parcel in) {
        akCode = in.readString();
        skCode = in.readString();
        hostPkg = in.readString();
        authUri = in.readString();
        authToken = in.readString();
    }

    public String getAkCode() {
        return akCode;
    }

    public void setAkCode(String akCode) {
        this.akCode = akCode;
    }

    public String getSkCode() {
        return skCode;
    }

    public void setSkCode(String skCode) {
        this.skCode = skCode;
    }

    public String getHostPkg() {
        return hostPkg;
    }

    public void setHostPkg(String hostPkg) {
        this.hostPkg = hostPkg;
    }

    public String getAuthUri() {
        return authUri;
    }

    public void setAuthUri(String authUri) {
        this.authUri = authUri;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public static final Creator<AuthInfo> CREATOR = new Creator<AuthInfo>() {
        @Override
        public AuthInfo createFromParcel(Parcel in) {
            return new AuthInfo(in);
        }

        @Override
        public AuthInfo[] newArray(int size) {
            return new AuthInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(akCode);
        dest.writeString(skCode);
        dest.writeString(hostPkg);
        dest.writeString(authUri);
        dest.writeString(authToken);
    }
}
