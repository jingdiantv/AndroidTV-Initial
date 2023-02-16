package com.zwn.user.ui.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zeewain.base.utils.GlideApp;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.databinding.AboutusFragmentBinding;
import com.zwn.user.ui.UserCenterViewModel;
import com.zwn.user.ui.UserCenterViewModelFactory;

public class AboutUsFragment extends Fragment {

    private AboutusFragmentBinding mBinding;
    private UserCenterViewModel mViewModel;

    public static AboutUsFragment newInstance() {
        return new AboutUsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = AboutusFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initObserve();
        mViewModel.reqAboutUsInfo();
    }

    private void initData() {
        mViewModel = new ViewModelProvider(this, new UserCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(UserCenterViewModel.class);
    }

    private void initObserve() {
        mViewModel.pReqAboutUsInfoState.observe(this, reqState -> {
            switch (reqState) {
                case SUCCESS:
                    GlideApp.with(this)
                            .load(mViewModel.pAboutUsInfo.wxOfficialAccounts)
                            .apply(CommonVariableCacheUtils.getInstance().getOptions13())
                            .error(R.mipmap.img_qr_code)
                            .into(mBinding.ivAboutusQrcode);
                    GlideApp.with(this)
                            .load(mViewModel.pAboutUsInfo.logo)
                            .error(R.mipmap.logo_company)
                            .into(mBinding.ivAboutusLogo);
                    mBinding.tvAboutusIntro.setText(mViewModel.pAboutUsInfo.slogan);
                    break;
                case FAILED:
                case ERROR:
                    GlideApp.with(this)
                            .load(R.mipmap.img_qr_code)
                            .apply(CommonVariableCacheUtils.getInstance().getOptions13())
                            .error(R.drawable.mine_list_favor_iv_error)
                            .into(mBinding.ivAboutusQrcode);
                    break;
            }
        });
    }
}
