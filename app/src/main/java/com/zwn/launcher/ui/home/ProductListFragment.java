package com.zwn.launcher.ui.home;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zeewain.base.model.LoadState;
import com.zeewain.base.model.TopGroupAction;
import com.zeewain.base.ui.OnTopGroupInteractionListener;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.launcher.MainActivity;
import com.zwn.launcher.MainViewModel;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.model.Footer;
import com.zwn.launcher.data.model.PagedPrdListLoadState;
import com.zwn.launcher.data.model.ProductListLoadState;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.presenter.ContentPresenterSelector;
import com.zwn.launcher.presenter.PagedListRow;
import com.zwn.launcher.presenter.PagedListRowPresenter;
import com.zwn.launcher.presenter.TypeMainMasterPresenter;
import com.zwn.launcher.presenter.TypeMainSlavePresenter;
import com.zwn.launcher.presenter.TypePagedRecommendPresenter;
import com.zwn.launcher.widgets.TabVerticalGridView;

import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "CategoryId";
    private static final String ARG_CATEGORY_Index = "CategoryIndex";
    private String mCategoryId;
    private int mCategoryIndex;
    private TabVerticalGridView mVerticalGridView;
    private View mRootView;
    private ArrayObjectAdapter mAdapter;
    private MainViewModel mViewModel;
    private MainActivity mActivity;
    private LoadingView loadingView;
    private LinearLayout noDataLayout;
    private NetworkErrView networkErrView;
    private boolean initFirst = true;
    private PagedPrdListLoadState usePagedPrdListLoadState;
    private ProductListLoadState useProductListLoadState;

    private OnTopGroupInteractionListener mListener;

    public static ProductListFragment newInstance(String categoryId, int categoryIndex) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putInt(ARG_CATEGORY_Index, categoryIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryId = getArguments().getString(ARG_CATEGORY_ID);
            mCategoryIndex = getArguments().getInt(ARG_CATEGORY_Index);
        }
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        if(mRootView == null){
            mRootView = inflater.inflate(R.layout.product_list_fragment, container, false);
            //AutoUtils.auto(mRootView);
            initView();
            initListener();
        }

        mViewModel.initCommonData(mCategoryId);
        return mRootView;
    }

    private void initView() {
        mVerticalGridView = mRootView.findViewById(R.id.tabVerticalGridView);
        loadingView = mRootView.findViewById(R.id.loadingView_product_list);
        noDataLayout = mRootView.findViewById(R.id.ll_product_list_no_data);
        networkErrView  = mRootView.findViewById(R.id.networkErrView_product);
        mVerticalGridView.setTabView(mActivity.getHorizontalGridView());
        mVerticalGridView.setGroup(mActivity.getGroup());
        mVerticalGridView.setVerticalSpacing(DisplayUtil.dip2px(mVerticalGridView.getContext(), 15));
        ContentPresenterSelector presenterSelector = new ContentPresenterSelector();

        PagedListRowPresenter pagedRowPresenter = new PagedListRowPresenter(getViewLifecycleOwner(), mCategoryId);
        pagedRowPresenter.setShadowEnabled(false);
        pagedRowPresenter.setSelectEffectEnabled(false);
        pagedRowPresenter.setKeepChildForeground(false);
        presenterSelector.addClassPresenter(PagedListRow.class, pagedRowPresenter, TypePagedRecommendPresenter.class);

        mAdapter = new ArrayObjectAdapter(presenterSelector);
        ItemBridgeAdapter itemBridgeAdapter = new ItemBridgeAdapter(mAdapter);
        mVerticalGridView.setAdapter(itemBridgeAdapter);
    }

    private void initListener() {
        mVerticalGridView.addOnScrollListener(onScrollListener);
        mVerticalGridView.addOnChildViewHolderSelectedListener(onSelectedListener);
        networkErrView.setRetryClickListener(() -> mViewModel.initCommonData(mCategoryId));
    }

    private final RecyclerView.OnScrollListener onScrollListener
            = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
                //当屏幕滚动且用户使用的触碰或手指还在屏幕上，停止加载图片
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    //由于用户的操作，屏幕产生惯性滑动，停止加载图片
                case RecyclerView.SCROLL_STATE_SETTLING:
                    GlideApp.with(mActivity).pauseRequests();
                    break;
                case RecyclerView.SCROLL_STATE_IDLE:
                    GlideApp.with(mActivity).resumeRequests();
            }
        }
    };

    private final OnChildViewHolderSelectedListener onSelectedListener
            = new OnChildViewHolderSelectedListener() {
        @Override
        public void onChildViewHolderSelected(RecyclerView parent,
                                              RecyclerView.ViewHolder child,
                                              int position, int subposition) {
            super.onChildViewHolderSelected(parent, child, position, subposition);
            if (mVerticalGridView == null) {
                return;
            }

            if (mVerticalGridView.isPressUp() && position == 0) {
                mVerticalGridView.scrollTo(0,0);
                mVerticalGridView.postDelayed(() -> mListener.onTopGroupInteraction(TopGroupAction.Show), 50);

            } else if (mVerticalGridView.isPressDown() && position == 1) {
                mListener.onTopGroupInteraction(TopGroupAction.Hide);
            }
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVerticalGridView != null) {
            mVerticalGridView.removeOnScrollListener(onScrollListener);
            mVerticalGridView.removeOnChildViewHolderSelectedListener(onSelectedListener);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.mldInitDataLoadState.observe(getViewLifecycleOwner(), productListLoadState -> {
            if(mCategoryId.equals(productListLoadState.categoryId)){
                if(LoadState.Loading == productListLoadState.loadState) {
                    networkErrView.setFocusable(false);
                    networkErrView.setVisibility(View.GONE);

                    mVerticalGridView.setVisibility(View.INVISIBLE);
                    noDataLayout.setVisibility(View.GONE);

                    loadingView.setVisibility(View.VISIBLE);
                    loadingView.startAnim();
                }else if (LoadState.Success == productListLoadState.loadState) {
                    networkErrView.setFocusable(false);
                    networkErrView.setVisibility(View.GONE);
                    loadingView.setVisibility(View.GONE);
                    loadingView.stopAnim();
                    mVerticalGridView.setVisibility(View.VISIBLE);
                    mAdapter.clear();

                    ProductListMo productModuleListMo = mViewModel.getProductModuleListFromCache(mCategoryId);
                    if(productModuleListMo != null && productModuleListMo.getRecords() != null && productModuleListMo.getRecords().size() > 0){
                        noDataLayout.setVisibility(View.GONE);
                        addMainMasterItem(productModuleListMo);
                        addMainSlaveItem(productModuleListMo);
                        ProductListMo productListMo = mViewModel.getProductListFromCache(mCategoryId);
                        if (productListMo != null) {
                            addPagedRecommendItem(productListMo);
                        }
                        addWithTryCatch(new Footer());
                    }else{
                        noDataLayout.setVisibility(View.VISIBLE);
                    }
                }else{
                    //error todo
                    networkErrView.setFocusable(true);
                    networkErrView.setVisibility(View.VISIBLE);

                    mVerticalGridView.setVisibility(View.INVISIBLE);
                    noDataLayout.setVisibility(View.GONE);

                    loadingView.setVisibility(View.GONE);
                    loadingView.stopAnim();
                }
            }
        });
        mViewModel.mldProductListLoadState.observe(getViewLifecycleOwner(), productListLoadState -> {
            if(mCategoryId.equals(productListLoadState.categoryId)) {
                if(productListLoadState.pageNum == 1 ){
                    usePagedPrdListLoadState = productListLoadState;
                    handleMulLoadState(productListLoadState, useProductListLoadState);
                }
            }
        });

        mViewModel.mldProductModuleListLoadState.observe(getViewLifecycleOwner(), productListLoadState -> {
            if(mCategoryId.equals(productListLoadState.categoryId)) {
                useProductListLoadState = productListLoadState;
                handleMulLoadState(productListLoadState, usePagedPrdListLoadState);
            }
        });

        mViewModel.mldSelectedTab.observe(getViewLifecycleOwner(), integer -> {
            if(integer == mCategoryIndex){
                if(mAdapter.size() > 2){
                    mAdapter.notifyItemRangeChanged(2,1);
                }
            }
        });

        mViewModel.mldNetConnected.observe(getViewLifecycleOwner(), aBoolean -> {
            if(!initFirst && !mViewModel.isCommonDataDone(mCategoryId)){
                mViewModel.initCommonData(mCategoryId);
            }
            initFirst = false;
        });
    }

    private void handleMulLoadState(ProductListLoadState plLoadState1, ProductListLoadState plLoadState2){

        if(plLoadState1 == null || plLoadState2 == null)
            return;
        if(plLoadState1.loadState == LoadState.Loading || plLoadState2.loadState == LoadState.Loading)
            return;

        if(plLoadState1.loadState == LoadState.Success && plLoadState2.loadState == LoadState.Success){
            mViewModel.mldInitDataLoadState.setValue(new ProductListLoadState(plLoadState1.categoryId, LoadState.Success));
        }else{
            mViewModel.mldInitDataLoadState.setValue(new ProductListLoadState(plLoadState1.categoryId, LoadState.Failed));
        }
    }

    private void addMainMasterItem(ProductListMo productListMo){
        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new TypeMainMasterPresenter());
        List<ProductListMo.Record> dataList = productListMo.getRecords();
        if (dataList != null && dataList.size() > 2) {
            dataList = dataList.subList(0, 2);
        }

        arrayObjectAdapter.addAll(0, dataList);
        ListRow listRow = new ListRow(arrayObjectAdapter);
        addWithTryCatch(listRow);
    }

    private void addMainSlaveItem(ProductListMo productListMo){
        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new TypeMainSlavePresenter());
        List<ProductListMo.Record> dataList = new ArrayList<>();
        dataList.add(getLatestRecord());

        List<ProductListMo.Record> recordsList = productListMo.getRecords();
        if (recordsList != null && recordsList.size() > 2) {
            for(int i=2; i<recordsList.size()&&i<5; i++){
                dataList.add(recordsList.get(i));
            }
        }

        arrayObjectAdapter.addAll(0, dataList);
        ListRow listRow = new ListRow(arrayObjectAdapter);
        addWithTryCatch(listRow);
    }

    private ProductListMo.Record getLatestRecord(){
        ProductListMo.Record record = new ProductListMo.Record();
        record.setSpuId(ProdConstants.LATEST_INTERACT_ITEM);
        return record;
    }

    private void addPagedRecommendItem(ProductListMo productListMo){
        ArrayObjectAdapter pagedRecommendAdapter = new ArrayObjectAdapter(new TypePagedRecommendPresenter());
        pagedRecommendAdapter.addAll(0, productListMo.getRecords());
        HeaderItem headerItemSix = new HeaderItem("为您推荐");
        PagedListRow listRowSix = new PagedListRow(headerItemSix, pagedRecommendAdapter);
        addWithTryCatch(listRowSix);
    }

    private void addWithTryCatch(Object item) {
        try {
            if (!mVerticalGridView.isComputingLayout()) {
                mAdapter.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}