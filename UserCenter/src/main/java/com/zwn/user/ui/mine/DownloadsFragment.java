package com.zwn.user.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.FocusHighlightHelper;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.databinding.MineDownFragmentBinding;
import com.zwn.user.presenter.DownloadsPresenter;
import com.zwn.user.ui.LoginCenterActivity;
import com.zwn.user.ui.UserCenterActivity;
import com.zwn.user.ui.UserCenterViewModel;
import com.zwn.user.ui.UserCenterViewModelFactory;
import com.zwn.user.utils.AndroidHelper;
import com.zwn.user.utils.CustomDialog;

import java.util.Objects;

public class DownloadsFragment extends Fragment {

    private static final String TAG = "DownloadsFragment";
    private static final int NUM_COLUMNS = 4;
    private static final int BACK_CODE = 100;

    private MineDownFragmentBinding mBinding;
    private UserCenterViewModel mViewModel;
    private UserCenterActivity mActivity;

    private ItemBridgeAdapter mItemBridgeAdapter;
    private ArrayObjectAdapter mArrayObjectAdapter;
    private DownloadsPresenter mPresenter;

    private boolean mOnDelete = false;

    public static DownloadsFragment newInstance() {
        return new DownloadsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = MineDownFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initView();
        initListener();
    }

    private void initData() {
        mActivity = (UserCenterActivity) Objects.requireNonNull(getActivity());
        mActivity.bindManagerService();
        mViewModel = new ViewModelProvider(this, new UserCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(UserCenterViewModel.class);
        mViewModel.getDownloadList();
        mPresenter = new DownloadsPresenter();
        mArrayObjectAdapter = new ArrayObjectAdapter(mPresenter);
        mArrayObjectAdapter.addAll(0, mViewModel.pDownloadInfoList);
        mItemBridgeAdapter = new ItemBridgeAdapter(mArrayObjectAdapter);
    }

    private void initView() {
        mBinding.tvMineDownTips.setText(AndroidHelper.getUserCenterEditTips(mActivity));
        mBinding.vgvMineDown.setNumColumns(NUM_COLUMNS);
        mBinding.vgvMineDown.setHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.src_dp_18));
        mBinding.vgvMineDown.setVerticalSpacing(getResources().getDimensionPixelSize(R.dimen.src_dp_23));
        mBinding.vgvMineDown.setAdapter(mItemBridgeAdapter);
        FocusHighlightHelper.setupBrowseItemFocusHighlight(mItemBridgeAdapter,
                FocusHighlight.ZOOM_FACTOR_LARGE, false);
        if (CommonVariableCacheUtils.getInstance().token.isEmpty()) {
            mBinding.includeMineDownNeedLogin.setVisibility(View.VISIBLE);
            mBinding.tvMineDownTitle.setVisibility(View.INVISIBLE);
            mActivity.findViewById(R.id.btn_need_login_base).requestFocus();
            return;
        }
        if (mArrayObjectAdapter.size() == 0) {
            showNothing();
        } else {
            mBinding.tvMineDownTips.setVisibility(View.VISIBLE);
        }
    }

    private void initListener() {
        mBinding.btnMineDownClear.setOnClickListener(v -> {
            new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                int res = mViewModel.clearDownload();
                if (res == 0) {
                    mViewModel.pDownloadInfoList.clear();
                    mArrayObjectAdapter.clear();
                    showNothing();
                    return;
                }
                String showText = mViewModel.pDownloadInfoList.get(res - 1).fileName + "删除失败";
                for (int i = res - 2; i >= 0; i--) {
                    mViewModel.pDownloadInfoList.remove(i);
                    mArrayObjectAdapter.removeItems(i, 1);
                }
                Toast.makeText(mActivity, showText, Toast.LENGTH_SHORT).show();
            }, () -> {

            }).setTitle("确定清空全部下载内容？").show();
        });

        mActivity.findViewById(R.id.btn_need_login_base).setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, LoginCenterActivity.class);
            startActivityForResult(intent, BACK_CODE);
        });

        mItemBridgeAdapter.setAdapterListener(new ItemBridgeAdapter.AdapterListener() {
            @Override
            public void onCreate(ItemBridgeAdapter.ViewHolder viewHolder) {
                viewHolder.itemView.setOnClickListener(v -> {
                    int pos = viewHolder.getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) {
                        return;
                    }
                    DownloadInfo downloadInfo = (DownloadInfo) mArrayObjectAdapter.get(pos);
                    String showTitle = "确定删除" + downloadInfo.fileName + "？";
                    if (mOnDelete) {
                        new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                            int res = mViewModel.delDownload(pos);
                            if (res > 0) {
                                mViewModel.pDownloadInfoList.remove(pos);
                                mArrayObjectAdapter.removeItems(pos, 1);
                                mActivity.remoteDeleteCall(downloadInfo.mainClassPath);
                            } else {
                                String text = mViewModel.pDownloadInfoList.get(pos).fileName + "删除失败";
                                Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
                            }
                            if (mArrayObjectAdapter.size() == 0) {
                                showNothing();
                            }
                        }, () -> {

                        }).setTitle(showTitle).show();
                    } else {
                        try {
                            Intent intent = new Intent(mActivity, Class.forName("com.zwn.launcher.ui.detail.DetailActivity"));
                            intent.putExtra("skuId", downloadInfo.extraId);
                            startActivity(intent);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void showNothing() {
        mOnDelete = false;
        mBinding.tvMineDownTips.setVisibility(View.INVISIBLE);
        mBinding.btnMineDownClear.setVisibility(View.INVISIBLE);
        mBinding.includeMineDownEmpty.setVisibility(View.VISIBLE);
        mBinding.tvMineDownTitle.setVisibility(View.INVISIBLE);
    }

    public void isOnDelete() {
        mOnDelete = !mOnDelete;
        mBinding.tvMineDownTips.setVisibility(mOnDelete ? View.INVISIBLE : View.VISIBLE);
        mBinding.btnMineDownClear.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);

        mPresenter.isOnDelete(mOnDelete);
        mArrayObjectAdapter.notifyArrayItemRangeChanged(0, mArrayObjectAdapter.size());
    }

    public int getItemSize() {
        return mArrayObjectAdapter.size();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BACK_CODE) {
            if (!CommonVariableCacheUtils.getInstance().token.isEmpty()) {
                mBinding.includeMineDownNeedLogin.setVisibility(View.INVISIBLE);
                mBinding.tvMineDownTitle.setVisibility(View.VISIBLE);
                mViewModel.getDownloadList();
                if (mViewModel.pDownloadInfoList.size() > 0) {
                    mBinding.tvMineDownTips.setVisibility(View.VISIBLE);
                    mArrayObjectAdapter.addAll(0, mViewModel.pDownloadInfoList);
                } else {
                    showNothing();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        mViewModel = null;
        super.onDestroyView();
    }
}
