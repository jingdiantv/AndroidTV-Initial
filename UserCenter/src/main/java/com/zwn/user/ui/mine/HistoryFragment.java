package com.zwn.user.ui.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.FocusHighlightHelper;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.model.ReqState;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.model.HistoryItem;
import com.zwn.user.databinding.MineHistFragmentBinding;
import com.zwn.user.presenter.HistoryPresenter;
import com.zwn.user.ui.LoginCenterActivity;
import com.zwn.user.ui.UserCenterActivity;
import com.zwn.user.ui.UserCenterViewModel;
import com.zwn.user.ui.UserCenterViewModelFactory;
import com.zwn.user.utils.AndroidHelper;
import com.zwn.user.utils.CustomDialog;

import java.util.Objects;

public class HistoryFragment extends Fragment {
    private final int BACK_DETAIL = 1;
    private final int BACK_LOGIN = 2;

    private Activity mActivity;
    private UserCenterViewModel mViewModel;
    private MineHistFragmentBinding mBinding;

    // private int mItemRadius = 26;
    private int mCornerRadius = 36;
    private int mBorderSide = 4;

    private final int mColumnNum = 4;
    // private int mHorizontalSpacing = 36;
    private int mVerticalSpacing = 46;
    private int mDelPosition = -1;

    private boolean mOnDelete = false;

    private HistoryPresenter mPresenter;
    private ItemBridgeAdapter mItemBridgeAdapter;
    private ArrayObjectAdapter mArrayObjectAdapter;

    private Animation mAnimation;

    private ImageView loadingImage;
    private TextView loadingText;
    private Button refreshButton;
    private Button loginButton;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = MineHistFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initView();
        initListener();
        initObserve();
        mViewModel.reqHistory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BACK_DETAIL && mViewModel.pEnterPosition != 0) {
            Log.d("测试", "再次请求数据");
            mViewModel.reqHistory();
        } else if (requestCode == BACK_LOGIN) {
            if (!CommonVariableCacheUtils.getInstance().token.isEmpty()) {
                mBinding.includeMineHistNeedLogin.setVisibility(View.INVISIBLE);
                forbidMove(true);
                mViewModel.reqHistory();
            }
        }
    }

    private void initData() {
        mActivity = Objects.requireNonNull(getActivity());
        mViewModel = new ViewModelProvider(this, new UserCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(UserCenterViewModel.class);
        mCornerRadius = AndroidHelper.fitScreen(mCornerRadius, mActivity);
        mBorderSide = AndroidHelper.fitScreen(mBorderSide, mActivity);

        mVerticalSpacing = AndroidHelper.fitScreen(mVerticalSpacing, mActivity);

        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
        mAnimation.setInterpolator(new LinearInterpolator());
    }

    private void initView() {
        mBinding.tvMineHistTips.setText(AndroidHelper.getUserCenterEditTips(mActivity));
        mBinding.vgvMineHist.setNextFocusRightId(R.id.vgv_mine_hist);
        mBinding.btnMineHistClear.setNextFocusLeftId(R.id.btn_mine_hist_clear);

        loadingImage = mBinding.getRoot().findViewById(R.id.iv_loading_dp);
        loadingText = mBinding.getRoot().findViewById(R.id.tv_loading_dp);
        refreshButton = mBinding.getRoot().findViewById(R.id.btn_req_failed_base);
        loginButton = mBinding.getRoot().findViewById(R.id.btn_need_login_base);

        loadingImage.setVisibility(View.INVISIBLE);
        loadingText.setVisibility(View.INVISIBLE);

        forbidMove(true);
    }

    private void initListener() {
        mBinding.btnMineHistClear.setOnClickListener(v -> {
            new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                forbidMove(false);
                mDelPosition = -1;
                mViewModel.clearHistory();
            }, () -> {

            }).setTitle("确定清空全部互动记录？").show();
        });

        refreshButton.setOnClickListener(v -> {
            forbidMove(true);
            mBinding.includeMineHistReqFailed.setVisibility(View.INVISIBLE);
            mViewModel.reqHistory();
        });

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, LoginCenterActivity.class);
            startActivityForResult(intent, BACK_LOGIN);
        });
    }

    private void initObserve() {
        mViewModel.pReqHistState.observe(this, state -> {
            recoverMove(true);
            switch (state) {
                case NEED_LOGIN:
                    showNeedLogin();
                    break;
                case SUCCESS:
                    if (mArrayObjectAdapter == null) {
                        int horizontalSpacing = getResources().getDimensionPixelSize(R.dimen.src_dp_18);
                        int itemRadius = getResources().getDimensionPixelSize(R.dimen.src_dp_13);
                        mBinding.vgvMineHist.setNumColumns(mColumnNum);

                        mBinding.vgvMineHist.setHorizontalSpacing(horizontalSpacing);
                        mBinding.vgvMineHist.setVerticalSpacing(mVerticalSpacing);

                        mPresenter = new HistoryPresenter(itemRadius);
                        mArrayObjectAdapter = new ArrayObjectAdapter(mPresenter);
                        mItemBridgeAdapter = new ItemBridgeAdapter(mArrayObjectAdapter);
                        mBinding.vgvMineHist.setAdapter(mItemBridgeAdapter);
                        mArrayObjectAdapter.addAll(0, mViewModel.pHistoryList);
                        FocusHighlightHelper.setupBrowseItemFocusHighlight(mItemBridgeAdapter,
                                FocusHighlight.ZOOM_FACTOR_MEDIUM, false);
                        setOnItemClickListener();
                    } else {
                        mArrayObjectAdapter.clear();
                        mArrayObjectAdapter.addAll(0, mViewModel.pHistoryList);
                    }
                    handleZeroHistory();
                    break;
                case FAILED:
                case ERROR:
                    if (mArrayObjectAdapter == null) {
                        showRefresh();
                    }
                    break;
            }
        });

        mViewModel.pDelHistState.observe(this, state -> {
            recoverMove(false);
            if (state == ReqState.SUCCESS) {
                if (mDelPosition >= 0) {
                    mArrayObjectAdapter.removeItems(mDelPosition, 1);
                } else {
                    mArrayObjectAdapter.clear();
                }
                handleZeroHistory();
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
                        String title = "确定删除" + ((HistoryItem) mArrayObjectAdapter.get(pos)).title + "？";
                        new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                            forbidMove(false);
                            mDelPosition = pos;
                            mViewModel.delHistory(mDelPosition);
                        }, () -> {

                        }).setTitle(title).show();
                    } else {
                        try {
                            Intent intent = new Intent(mActivity, Class.forName("com.zwn.launcher.ui.detail.DetailActivity"));
                            intent.putExtra("skuId", mViewModel.pHistoryList.get(pos).skuId);
                            mViewModel.pEnterPosition = pos;
                            startActivityForResult(intent, BACK_DETAIL);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void handleZeroHistory() {
        if (mViewModel.pHistoryList.size() == 0) {
            mOnDelete = false;
            mBinding.btnMineHistClear.setVisibility(View.INVISIBLE);
            mBinding.tvMineHistTips.setVisibility(View.INVISIBLE);
            mBinding.includeMineHistEmpty.setVisibility(View.VISIBLE);
            mBinding.tvMineHistTitle.setVisibility(View.INVISIBLE);
        } else {
            mBinding.tvMineHistTitle.setVisibility(View.VISIBLE);
            mBinding.tvMineHistTips.setVisibility(mOnDelete ? View.INVISIBLE : View.VISIBLE);
        }
    }

    private void showRefresh() {
        mBinding.includeMineHistReqFailed.setVisibility(View.VISIBLE);
        mBinding.tvMineHistTips.setVisibility(View.INVISIBLE);
        mBinding.btnMineHistClear.setVisibility(View.INVISIBLE);
        refreshButton.requestFocus();
    }

    private void showNeedLogin() {
        mBinding.includeMineHistNeedLogin.setVisibility(View.VISIBLE);
        mBinding.tvMineHistTips.setVisibility(View.INVISIBLE);
        mBinding.btnMineHistClear.setVisibility(View.INVISIBLE);
        loginButton.requestFocus();
    }

    private void forbidMove(boolean withText) {
        ((UserCenterActivity) mActivity).isLoading(true);
        loadingImage.setVisibility(View.VISIBLE);
        loadingImage.setAnimation(mAnimation);
        mBinding.vgvMineHist.setClickable(false);
        mBinding.btnMineHistClear.setClickable(false);
        if (withText) {
            loadingText.setVisibility(View.VISIBLE);
        }
    }

    private void recoverMove(boolean withText) {
        ((UserCenterActivity) mActivity).isLoading(false);
        loadingImage.clearAnimation();
        loadingImage.setVisibility(View.INVISIBLE);
        mBinding.vgvMineHist.setClickable(true);
        mBinding.btnMineHistClear.setClickable(true);
        if (withText) {
            loadingText.setVisibility(View.INVISIBLE);
        }
    }

    public void isOnDelete() {
        mOnDelete = !mOnDelete;
        mBinding.tvMineHistTips.setVisibility(mOnDelete ? View.INVISIBLE : View.VISIBLE);
        mBinding.btnMineHistClear.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);

        mPresenter.isOnDelete(mOnDelete);
        mArrayObjectAdapter.notifyArrayItemRangeChanged(0, mArrayObjectAdapter.size());
    }

    public int getHistNum() {
        return mViewModel.pHistoryList.size();
    }
}