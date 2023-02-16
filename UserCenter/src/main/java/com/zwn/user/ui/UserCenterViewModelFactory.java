package com.zwn.user.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zwn.user.data.UserRepository;

public class UserCenterViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository mUserRepository;
    private final Context mContext;

    public UserCenterViewModelFactory(UserRepository userRepository, Context context) {
        mUserRepository = userRepository;
        mContext = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserCenterViewModel(mUserRepository, mContext);
    }
}
