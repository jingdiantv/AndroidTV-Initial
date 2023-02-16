package com.zwn.user.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zwn.user.databinding.LoginSelectFragmentBinding;
import com.zwn.user.ui.LoginCenterActivity;

import java.util.Objects;

public class SelectLoginWaysFragment extends Fragment {

    private LoginSelectFragmentBinding binding;
    private AccountLoginFragment accountLoginFragment;
    private SmsCodeLoginFragment smsCodeLoginFragment;

    public static SelectLoginWaysFragment newInstance() {
        return new SelectLoginWaysFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LoginSelectFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initViewListener();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            binding.btnLoginSlMsg.requestFocus();
        }
    }

    private void initData() {
        accountLoginFragment = AccountLoginFragment.newInstance();
        smsCodeLoginFragment = SmsCodeLoginFragment.newInstance();
        binding.btnLoginSlMsg.requestFocus();
    }

    private void initViewListener() {
        binding.btnLoginSlMsg.setOnClickListener(v -> {
            ((LoginCenterActivity) Objects.requireNonNull(getActivity())).replaceFragment(
                    smsCodeLoginFragment, true, false);
        });
        binding.btnLoginSlPwd.setOnClickListener(v -> {
            ((LoginCenterActivity) Objects.requireNonNull(getActivity())).replaceFragment(
                    accountLoginFragment, true, false);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        accountLoginFragment = null;
        smsCodeLoginFragment = null;
    }
}
