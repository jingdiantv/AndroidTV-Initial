package com.zwn.launcher.ui.mine;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.model.MineCommonMo;
import com.zeewain.base.model.MineHeader;
import com.zeewain.base.model.ReqState;
import com.zeewain.base.model.TopGroupAction;
import com.zeewain.base.ui.OnTopGroupInteractionListener;
import com.zeewain.base.utils.CommonUtils;
import com.zwn.launcher.MainActivity;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.databinding.MineFragmentBinding;
import com.zwn.launcher.presenter.MineDownloadsPresenter;
import com.zwn.launcher.presenter.MineFavoritesPresenter;
import com.zwn.launcher.presenter.MineFooterPresenter;
import com.zwn.launcher.presenter.MineHeaderPresenter;
import com.zwn.launcher.presenter.MineHistoryPresenter;
import com.zwn.launcher.presenter.MinePresenterSelector;
import com.zwn.user.data.UserRepository;
import com.zwn.user.ui.UserCenterViewModel;
import com.zwn.user.ui.UserCenterViewModelFactory;

public class MineFragment extends Fragment {
    private final static String TAG = "MineFragment";

    private final static int MAX_ARRAY_NUM = 5;

    private MineFragmentBinding mBinding;
    private MainActivity mActivity;

    private ArrayObjectAdapter mArrayObjectAdapter;
    private ItemBridgeAdapter mItemBridgeAdapter;

    private UserCenterViewModel mViewModel;

    public static MineFragment newInstance() {
        return new MineFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnTopGroupInteractionListener) {
            mListener = (OnTopGroupInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTopGroupInteractionListener");
        }
        mActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = MineFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initData();
        initObserver();
        addData();
    }

    private void initView() {
        MinePresenterSelector minePresenterSelector = new MinePresenterSelector(getResources());
        mArrayObjectAdapter = new ArrayObjectAdapter(minePresenterSelector);
        mItemBridgeAdapter = new ItemBridgeAdapter(mArrayObjectAdapter);
        mBinding.vgvMine.setTabView(mActivity.getHorizontalGridView());
        mBinding.vgvMine.setGroup(mActivity.getGroup());
        mBinding.vgvMine.addOnChildViewHolderSelectedListener(onSelectedListener);
        mBinding.vgvMine.setAdapter(mItemBridgeAdapter);
    }

    private void initData() {
        mViewModel = new ViewModelProvider(this, new UserCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(UserCenterViewModel.class);
    }

    private void initObserver() {
        mViewModel.pReqFavoritesState.observe(this, state -> {
            if (state == ReqState.ERROR) {
//                Toast.makeText(getContext(), "网络或服务器异常", Toast.LENGTH_SHORT).show();
            }
            showData();
        });

        mViewModel.pReqHistState.observe(this, state -> {
            if (state == ReqState.ERROR) {
//                Toast.makeText(getContext(), "网络或服务器异常", Toast.LENGTH_SHORT).show();
            }
            showData();
        });
    }

    private int mDataReady = 0;
    private void showData() {
        mDataReady++;
        if (mDataReady >= 2) {
            mDataReady = 0;
            addData();
        }
    }

    private void addData() {
        addHeader();
        addHistory();
        addFavorites();
        addDownloads();
        addFooter();
    }

    private void reqData() {
        mViewModel.pHistoryList.clear();
        mViewModel.pFavoritesList.clear();
        mViewModel.pDownloadInfoList.clear();

        mViewModel.reqHistory();
        mViewModel.reqFavoritesList();
        mViewModel.getDownloadList();
    }

    private OnTopGroupInteractionListener mListener;

    private final OnChildViewHolderSelectedListener onSelectedListener
            = new OnChildViewHolderSelectedListener() {
        @Override
        public void onChildViewHolderSelected(RecyclerView parent,
                                              RecyclerView.ViewHolder child,
                                              int position, int subposition) {
            super.onChildViewHolderSelected(parent, child, position, subposition);
            if (mBinding == null) {
                return;
            }
            if (mBinding.vgvMine.isPressUp() && position == 0) {
                mBinding.vgvMine.scrollTo(0,0);
                mBinding.vgvMine.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onTopGroupInteraction(TopGroupAction.Show);
                    }
                }, 50);

            } else if (mBinding.vgvMine.isPressDown() && position == 1) {
                mListener.onTopGroupInteraction(TopGroupAction.Hide);
            }
        }
    };

    private void addHeader() {
        String userAccount = CommonUtils.getUserInfo();
        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new MineHeaderPresenter());
        if (userAccount == null || userAccount.isEmpty()) {
            arrayObjectAdapter.add(new MineHeader("未登录", R.mipmap.ic_head));
            ListRow listRow = new ListRow(arrayObjectAdapter);
            addData(listRow, 0);
        } else {
            arrayObjectAdapter.add(new MineHeader(userAccount, R.mipmap.img_user_avatar));
            ListRow listRow = new ListRow(arrayObjectAdapter);
            addData(listRow, 0);
        }
    }

    private void addHistory() {
        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new MineHistoryPresenter());
        for (int i = 0; i < mViewModel.pHistoryList.size(); i++) {
            if (arrayObjectAdapter.size() >= 2) {
                break;
            }
            MineCommonMo model = new MineCommonMo();
            model.imgUrl = mViewModel.pHistoryList.get(i).url;
            model.skuId = mViewModel.pHistoryList.get(i).skuId;
            model.type = ProdConstants.MINE_PRODUCT;
            arrayObjectAdapter.add(model);
        }
        MineCommonMo model = new MineCommonMo();
        model.type = ProdConstants.INTERACTIVE_RECORD;
        arrayObjectAdapter.add(model);
        HeaderItem headerItem = new HeaderItem("互动记录");
        ListRow listRow = new ListRow(headerItem, arrayObjectAdapter);
        addData(listRow, 1);
    }

    private void addFavorites() {
        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new MineFavoritesPresenter());
        for (int i = 0; i < mViewModel.pFavoritesList.size(); i++) {
            if (arrayObjectAdapter.size() >= 2) {
                break;
            }
            MineCommonMo model = new MineCommonMo();
            model.imgUrl = mViewModel.pFavoritesList.get(i).url;
            model.skuId = mViewModel.pFavoritesList.get(i).objId;
            model.type = ProdConstants.MINE_PRODUCT;
            arrayObjectAdapter.add(model);
        }
        MineCommonMo model = new MineCommonMo();
        model.type = ProdConstants.MY_FAVORITES;
        arrayObjectAdapter.add(model);
        HeaderItem headerItem = new HeaderItem("全部收藏");
        ListRow listRow = new ListRow(headerItem, arrayObjectAdapter);
        addData(listRow, 2);
    }

    private void addDownloads() {
        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new MineDownloadsPresenter());
        for (int i = 0; i < mViewModel.pDownloadInfoList.size(); i++) {
            if (arrayObjectAdapter.size() >= 2) break;
            MineCommonMo model = new MineCommonMo();
            model.imgUrl = mViewModel.pDownloadInfoList.get(i).fileImgUrl;
            model.skuId = mViewModel.pDownloadInfoList.get(i).extraId;
            model.type = ProdConstants.MINE_PRODUCT;
            arrayObjectAdapter.add(model);
        }
        MineCommonMo model = new MineCommonMo();
        model.type = ProdConstants.MY_DOWNLOADS;
        arrayObjectAdapter.add(model);
        HeaderItem headerItem = new HeaderItem("全部下载");
        ListRow listRow = new ListRow(headerItem, arrayObjectAdapter);
        addData(listRow, 3);
    }

    private void addFooter() {
        MineCommonMo updateModel = new MineCommonMo();
        MineCommonMo aboutUsModel = new MineCommonMo();

        updateModel.itemId = R.mipmap.ic_version_update;
        aboutUsModel.itemId = R.mipmap.icon_about_my;

        updateModel.type = ProdConstants.VERSION_UPDATE;
        aboutUsModel.type = ProdConstants.ABOUT_US;

        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new MineFooterPresenter());
        arrayObjectAdapter.add(updateModel);
        arrayObjectAdapter.add(aboutUsModel);

        HeaderItem headerItem = new HeaderItem("其他功能");
        ListRow listRow = new ListRow(headerItem, arrayObjectAdapter);
        if (mArrayObjectAdapter.size() < MAX_ARRAY_NUM) {
            mArrayObjectAdapter.add(listRow);
        }
    }

    private void addData(ListRow listRow, int index) {
        if (mArrayObjectAdapter.size() == MAX_ARRAY_NUM) {
            if (index == 0) {
                ListRow oldListRow = (ListRow) mArrayObjectAdapter.get(index);
                MineHeader header1 = (MineHeader) listRow.getAdapter().get(0);
                MineHeader header2 = (MineHeader) oldListRow.getAdapter().get(0);
                if (!header1.userAccount.equals(header2.userAccount)) {
                    mArrayObjectAdapter.replace(index, listRow);
                }
            } else {
                if (!listRowEquals(listRow, (ListRow) mArrayObjectAdapter.get(index))) {
                    mArrayObjectAdapter.replace(index, listRow);
                }
            }
        } else {
            mArrayObjectAdapter.add(listRow);
        }
    }

    private boolean listRowEquals(ListRow listRow1, ListRow listRow2) {
        ArrayObjectAdapter aoa1 = (ArrayObjectAdapter) listRow1.getAdapter();
        ArrayObjectAdapter aoa2 = (ArrayObjectAdapter) listRow2.getAdapter();
        if (aoa1.size() != aoa2.size()) {
            return false;
        }
        int size = aoa1.size();
        for (int i = 0; i < size - 1; i++) {
            MineCommonMo model1 = (MineCommonMo) aoa1.get(i);
            MineCommonMo model2 = (MineCommonMo) aoa2.get(i);
            if (!model1.skuId.equals(model2.skuId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "MineFragment onResume");
        reqData();
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        if (mBinding != null) {
            mBinding.vgvMine.removeOnChildViewHolderSelectedListener(onSelectedListener);
        }
        mViewModel = null;
        mBinding = null;
        super.onDestroyView();
    }
}
