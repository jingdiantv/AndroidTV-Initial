// IRemoteService.aidl
package com.zee.unity;

parcelable AuthInfo;

interface IRemoteService {
    AuthInfo getAuthInfo();
}