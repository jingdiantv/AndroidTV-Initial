package com.zwn.launcher.ui.home;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.model.TopGroupAction;
import com.zeewain.base.ui.OnTopGroupInteractionListener;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.utils.ZeeWainGson;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zeewain.search.data.model.CourseInfo;
import com.zeewain.search.data.model.RecommendInfo;
import com.zwn.launcher.MainActivity;
import com.zwn.launcher.MainViewModel;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.model.Footer;
import com.zwn.launcher.data.model.ProductListLoadState;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.presenter.ContentPresenterSelector;
import com.zwn.launcher.presenter.ModuleListPagedPresenter;
import com.zwn.launcher.presenter.ModuleType2PagedListRow;
import com.zwn.launcher.presenter.ModuleType7PagedListRow;
import com.zwn.launcher.presenter.TypeMainMasterPresenter;
import com.zwn.launcher.presenter.TypeMainSlavePresenter;
import com.zwn.launcher.presenter.TypePagedRecommendPresenter;
import com.zwn.launcher.presenter.TypeRecommendPresenter;
import com.zwn.launcher.widgets.TabVerticalGridView;

import java.util.ArrayList;
import java.util.List;

public class CareListFragment extends Fragment {

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

    private OnTopGroupInteractionListener mListener;

    public static CareListFragment newInstance(String categoryId, int categoryIndex) {
        CareListFragment fragment = new CareListFragment();
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
            initView();
            initListener();
        }

        mViewModel.initCareModuleList(mCategoryId);
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

        ModuleListPagedPresenter pagedRowPresenter = new ModuleListPagedPresenter(getViewLifecycleOwner(), mCategoryId, ProdConstants.Module.TYPE_2);
        pagedRowPresenter.setShadowEnabled(false);
        pagedRowPresenter.setSelectEffectEnabled(false);
        pagedRowPresenter.setKeepChildForeground(false);
        presenterSelector.addClassPresenter(ModuleType2PagedListRow.class, pagedRowPresenter, TypePagedRecommendPresenter.class);

        pagedRowPresenter = new ModuleListPagedPresenter(getViewLifecycleOwner(), mCategoryId, ProdConstants.Module.TYPE_7);
        pagedRowPresenter.setShadowEnabled(false);
        pagedRowPresenter.setSelectEffectEnabled(false);
        pagedRowPresenter.setKeepChildForeground(false);
        presenterSelector.addClassPresenter(ModuleType7PagedListRow.class, pagedRowPresenter, TypePagedRecommendPresenter.class);

        mAdapter = new ArrayObjectAdapter(presenterSelector);
        ItemBridgeAdapter itemBridgeAdapter = new ItemBridgeAdapter(mAdapter);
        mVerticalGridView.setAdapter(itemBridgeAdapter);
    }

    private void initListener() {
        mVerticalGridView.addOnScrollListener(onScrollListener);
        mVerticalGridView.addOnChildViewHolderSelectedListener(onSelectedListener);
        networkErrView.setRetryClickListener(() -> mViewModel.initCareModuleList(mCategoryId));
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
                //mListener.onTopGroupInteraction(TopGroupAction.Show);
                mVerticalGridView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onTopGroupInteraction(TopGroupAction.Show);
                    }
                }, 50);
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
        mViewModel.mldCareModuleListLoadState.observe(getViewLifecycleOwner(), productListLoadState -> {
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

                    ProductListMo productModuleListMo = mViewModel.getCareModuleListFromCache(ProdConstants.Module.TYPE_5);

                    saveToCache(productModuleListMo);

                    if(productModuleListMo != null && productModuleListMo.getRecords() != null && productModuleListMo.getRecords().size() > 0){
                        addMainMasterItem(productModuleListMo);
                        addMainSlaveItem(productModuleListMo);

                        ProductListMo productListMo = mViewModel.getCareModuleListFromCache(ProdConstants.Module.TYPE_2);
                        if (productListMo != null && productListMo.getRecords() != null && productListMo.getRecords().size() > 0) {
                            //addRecommendItem(productListMo, "畅玩互动");
                            addPagedRecommendType2Item(productListMo, "畅玩互动");
                        }

                        productListMo = mViewModel.getCareModuleListFromCache(ProdConstants.Module.TYPE_7);
                        if (productListMo != null && productListMo.getRecords() != null && productListMo.getRecords().size() > 0) {
                            //addRecommendItem(productListMo, "儿童乐园");
                            addPagedRecommendType7Item(productListMo, "儿童乐园");
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

        mViewModel.mldSelectedTab.observe(getViewLifecycleOwner(), integer -> {
            if(integer == mCategoryIndex){
                if(mAdapter.size() > 2){
                    mAdapter.notifyItemRangeChanged(2,mAdapter.size() - 2);
                }
            }
        });

        mViewModel.mldNetConnected.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ProductListLoadState productListLoadState = mViewModel.mldCareModuleListLoadState.getValue();
                if(productListLoadState!= null && productListLoadState.loadState == LoadState.Failed){
                    mViewModel.initCareModuleList(mCategoryId);
                }
            }
        });
    }

    private void saveToCache(ProductListMo productModuleListMo) {
        List<ProductListMo.Record> records = productModuleListMo.getRecords();
        List<CourseInfo> courseInfoList=new ArrayList<>();
        for (int i=0;i<records.size();i++){
            ProductListMo.Record record = records.get(i);
            String skuId = record.getSkuId();
            String productTitle = record.getProductTitle();
            String productImg = record.getProductImg();
            CourseInfo courseInfo = new CourseInfo(skuId, productTitle, productImg);
            courseInfoList.add(courseInfo);
        }
        RecommendInfo recommendInfo=new RecommendInfo();
        recommendInfo.setCourseInfoList(courseInfoList);
        SPUtils.getInstance().put(SharePrefer.recommendInfo, ZeeWainGson.getInstance().toJson(recommendInfo));
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

    private void addRecommendItem(ProductListMo productListMo, String headerTitle){
        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new TypeRecommendPresenter());
        arrayObjectAdapter.addAll(0, productListMo.getRecords());
        HeaderItem headerItemSix = new HeaderItem(headerTitle);
        ListRow listRowSix = new ListRow(headerItemSix, arrayObjectAdapter);
        addWithTryCatch(listRowSix);
    }

    private void addPagedRecommendType2Item(ProductListMo productListMo, String headerTitle){
        ArrayObjectAdapter pagedRecommendAdapter = new ArrayObjectAdapter(new TypePagedRecommendPresenter());
        pagedRecommendAdapter.addAll(0, productListMo.getRecords());
        HeaderItem headerItemSix = new HeaderItem(headerTitle);
        ModuleType2PagedListRow listRowSix = new ModuleType2PagedListRow(headerItemSix, pagedRecommendAdapter);
        addWithTryCatch(listRowSix);
    }

    private void addPagedRecommendType7Item(ProductListMo productListMo, String headerTitle){
        ArrayObjectAdapter pagedRecommendAdapter = new ArrayObjectAdapter(new TypePagedRecommendPresenter());
        pagedRecommendAdapter.addAll(0, productListMo.getRecords());
        HeaderItem headerItemSix = new HeaderItem(headerTitle);
        ModuleType7PagedListRow listRowSix = new ModuleType7PagedListRow(headerItemSix, pagedRecommendAdapter);
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