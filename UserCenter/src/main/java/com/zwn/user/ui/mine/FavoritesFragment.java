package com.zwn.user.ui.mine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.model.ReqState;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.databinding.MineListFragmentBinding;
import com.zwn.user.presenter.FavoritesPresenter;
import com.zwn.user.ui.LoginCenterActivity;
import com.zwn.user.ui.UserCenterActivity;
import com.zwn.user.ui.UserCenterViewModel;
import com.zwn.user.ui.UserCenterViewModelFactory;
import com.zwn.user.utils.AndroidHelper;
import com.zwn.user.utils.CustomDialog;

import java.util.Objects;

public class FavoritesFragment extends Fragment {

    private final String TAG = "CollectFragment";
    private final int BACK_REFRESH = 1;
    private final int BACK_LOGIN = 2;

    private MineListFragmentBinding mBinding;
    private UserCenterViewModel mViewModel;
    private Animation mAnimation;
    private Activity mActivity;

    private boolean mOnDelete = false;
    private boolean clickAble = true;

    private ArrayObjectAdapter mArrayObjectAdapter;
    private ItemBridgeAdapter mItemBridgeAdapter;
    private FavoritesPresenter mPresenter;

    private final int mColumn = 1;
    private int mDelPosition = -1;

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    private Button refreshButton;
    private ImageView loadingImage;
    private TextView loadingText;
    private Button loginButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = MineListFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initView();
        initListener();
        initObserve();
        mViewModel.reqFavoritesList();
    }

    private void initData() {
        mActivity = Objects.requireNonNull(getActivity());
        mViewModel = new ViewModelProvider(this, new UserCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(UserCenterViewModel.class);

        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
        mAnimation.setInterpolator(new LinearInterpolator());

        refreshButton = mBinding.getRoot().findViewById(R.id.btn_req_failed_base);
        loadingImage = mBinding.getRoot().findViewById(R.id.iv_loading_dp);
        loadingText = mBinding.getRoot().findViewById(R.id.tv_loading_dp);
        loginButton = mBinding.getRoot().findViewById(R.id.btn_need_login_base);
    }

    private void initView() {
        mBinding.tvMineListTips.setText(AndroidHelper.getUserCenterEditTips(mActivity));
        loadingImage.setVisibility(View.INVISIBLE);
        loadingText.setVisibility(View.INVISIBLE);
        forbidMove(true);
    }

    private void initListener() {
        mBinding.btnMineListClear.setOnClickListener(v -> {
            new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                forbidMove(false);
                mDelPosition = -1;
                mViewModel.clearFavorites();
            }, () -> {

            }).setTitle("确定清空全部收藏内容？").show();
        });

        refreshButton.setOnClickListener(v -> {
            forbidMove(true);
            mBinding.inclMineListReqFailed.setVisibility(View.INVISIBLE);
            mViewModel.reqFavoritesList();
        });

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, LoginCenterActivity.class);
            startActivityForResult(intent, BACK_LOGIN);
        });
    }

    private void initObserve() {
        mViewModel.pReqFavoritesState.observe(this, state -> {
            recoverMove(true);
            switch (state) {
                case NEED_LOGIN:
                    showNeedLogin();
                    break;
                case SUCCESS:
                    mBinding.tvMineListTips.setVisibility(View.VISIBLE);
                    if (mItemBridgeAdapter == null) {
                        mBinding.vgvMineList.setNumColumns(mColumn);
                        int verticalSpacing = getResources().getDimensionPixelSize(R.dimen.src_dp_7);
                        mBinding.vgvMineList.setVerticalSpacing(verticalSpacing);

                        int imageRadius = getResources().getDimensionPixelSize(R.dimen.src_dp_13);
                        mPresenter = new FavoritesPresenter(imageRadius);
                        mArrayObjectAdapter = new ArrayObjectAdapter(mPresenter);
                        mItemBridgeAdapter = new ItemBridgeAdapter(mArrayObjectAdapter);
                        mBinding.vgvMineList.setAdapter(mItemBridgeAdapter);
                        mArrayObjectAdapter.addAll(0, mViewModel.pFavoritesList);
                        setOnItemClickListener();
                    } else {
                        mArrayObjectAdapter.clear();
                        mArrayObjectAdapter.addAll(0, mViewModel.pFavoritesList);
                    }
                    handleEmpty();
                    break;
                case ERROR:
                case FAILED:
                    if (mArrayObjectAdapter == null) {
                        showRefresh();
                    }
                    break;
            }
        });

        mViewModel.pDelFavoritesState.observe(this, state -> {
            recoverMove(false);
            if (state == ReqState.SUCCESS) {
                if (mDelPosition == -1) {
                    mArrayObjectAdapter.clear();
                } else if (mViewModel.pFavoritesList.size() == mArrayObjectAdapter.size() - 1) {
                    mArrayObjectAdapter.removeItems(mDelPosition, 1);
                    mDelPosition = -1;
                } else if (mViewModel.pFavoritesList.size() == mArrayObjectAdapter.size()) {
                    mArrayObjectAdapter.move(mDelPosition, 0);
                    mDelPosition = -1;
                }
                handleEmpty();
            }
        });
    }

    private void setOnItemClickListener() {
        mItemBridgeAdapter.setAdapterListener(new ItemBridgeAdapter.AdapterListener() {
            @Override
            public void onCreate(ItemBridgeAdapter.ViewHolder viewHolder) {
                viewHolder.itemView.setOnClickListener(v -> {
                    int pos = viewHolder.getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) {
                        return;
                    }
                    if (mOnDelete) {
                        if (!clickAble) {
                            return;
                        }
                        String showTitle = "确定删除" + mViewModel.pFavoritesList.get(pos).title + "？";
                        new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                            forbidMove(false);
                            mDelPosition = pos;
                            mViewModel.delFavorites(mDelPosition);
                        }, () -> {

                        }).setTitle(showTitle).show();
                    } else {
                        try {
                            Intent intent = new Intent(mActivity, Class.forName("com.zwn.launcher.ui.detail.DetailActivity"));
                            intent.putExtra("skuId", mViewModel.pFavoritesList.get(pos).objId);
                            mViewModel.pEnterPosition = pos;
                            mDelPosition = pos;
                            startActivityForResult(intent, BACK_REFRESH);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void showNothing() {
        mBinding.inclMineListZeroFavor.setVisibility(View.VISIBLE);
        mBinding.tvMineListTips.setVisibility(View.INVISIBLE);
        mBinding.btnMineListClear.setVisibility(View.INVISIBLE);
    }

    private void showRefresh() {
        mBinding.inclMineListReqFailed.setVisibility(View.VISIBLE);
        mBinding.tvMineListTips.setVisibility(View.INVISIBLE);
        mBinding.btnMineListClear.setVisibility(View.INVISIBLE);
        refreshButton.requestFocus();
    }

    private void showNeedLogin() {
        mBinding.inclMineListNeedLogin.setVisibility(View.VISIBLE);
        mBinding.tvMineListTips.setVisibility(View.INVISIBLE);
        mBinding.btnMineListClear.setVisibility(View.INVISIBLE);
        loginButton.requestFocus();
    }

    private void handleEmpty() {
        if (mViewModel.pFavoritesList.size() == 0) {
            mBinding.tvMineListTitle.setVisibility(View.INVISIBLE);
            showNothing();
        } else {
            mBinding.tvMineListTitle.setVisibility(View.VISIBLE);
        }
    }

    private void forbidMove(boolean withText) {
        clickAble = false;
        ((UserCenterActivity) mActivity).isLoading(true);
        loadingImage.setVisibility(View.VISIBLE);
        loadingImage.setAnimation(mAnimation);
        mBinding.vgvMineList.setClickable(false);
        mBinding.btnMineListClear.setClickable(false);
        if (withText) {
            loadingText.setVisibility(View.VISIBLE);
        }
    }

    private void recoverMove(boolean withText) {
        clickAble = true;
        ((UserCenterActivity) mActivity).isLoading(false);
        loadingImage.clearAnimation();
        loadingImage.setVisibility(View.INVISIBLE);
        mBinding.vgvMineList.setClickable(true);
        mBinding.btnMineListClear.setClickable(true);
        if (withText) {
            loadingText.setVisibility(View.INVISIBLE);
        }
    }

    public int getFavoritesNum() {
        return mViewModel.pFavoritesList.size();
    }

    public void showDelButton() {
        mOnDelete = !mOnDelete;
        mBinding.tvMineListTips.setVisibility(mOnDelete ? View.INVISIBLE : View.VISIBLE);
        mBinding.btnMineListClear.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);

        mPresenter.isOnDelete(mOnDelete);
        mArrayObjectAdapter.notifyArrayItemRangeChanged(0, mArrayObjectAdapter.size());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BACK_REFRESH) {
            mViewModel.reqFavoritesList();
        } else if (requestCode == BACK_LOGIN) {
            if (!CommonVariableCacheUtils.getInstance().token.isEmpty()) {
                mBinding.inclMineListNeedLogin.setVisibility(View.INVISIBLE);
                forbidMove(true);
                mViewModel.reqFavoritesList();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
