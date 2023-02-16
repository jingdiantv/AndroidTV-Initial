package com.zwn.user.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zeewain.base.utils.NetworkUtil;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.databinding.LoginSignoutFragmentBinding;
import com.zwn.user.ui.LoginCenterViewModel;
import com.zwn.user.ui.LoginCenterViewModelFactory;

import java.util.Objects;

public class SignOutFragment extends Fragment {
    private LoginSignoutFragmentBinding binding;
    private LoginCenterViewModel mViewModel;

    public static SignOutFragment newInstance() {
        return new SignOutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LoginSignoutFragmentBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this, new LoginCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(LoginCenterViewModel.class);
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
            initData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViewListener() {
        binding.btnSignout.setOnClickListener(v -> {
            if(NetworkUtil.isNetworkAvailable(v.getContext())) {
                mViewModel.removeUserInfo();
                // ((LoginCenterActivity) Objects.requireNonNull(getActivity())).showSelectFragment();
                Objects.requireNonNull(getActivity()).finish();
            }else{
                Toast.makeText(v.getContext(), "网络连接异常！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        assert getArguments() != null;
        String userAccount = getArguments().getString(UserCenterConf.USER_ACCOUNT_KEY);
        binding.tvSignoutAcct.setText(userAccount);

        binding.tvSignoutAcct.requestFocus();
    }
}
