package com.zeewain.search.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.FocusHighlightHelper;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.search.R;
import com.example.search.databinding.ActivitySearchBinding;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.search.adapter.SearchTipsAdapter;
import com.zeewain.search.conf.Constant;
import com.zeewain.search.data.SearchRepository;
import com.zeewain.search.data.model.CourseInfo;
import com.zeewain.search.model.CommonSearchModel;
import com.zeewain.search.presenter.SearchResultPresenter;
import com.zeewain.search.utils.DimensionUtils;

@SuppressLint("NotifyDataSetChanged")
public class SearchActivity extends BaseActivity {
    private final static String TAG = "SearchActivity";

    private ActivitySearchBinding mBinding;
    private SearchViewModel mViewModel;

    private SearchResultPresenter mSearchResultPresenter;
    private ArrayObjectAdapter mArrayObjectAdapter;
    private ItemBridgeAdapter mItemBridgeAdapter;

    private SearchTipsAdapter mSearchTipsAdapter;

    private ObjectAnimator mKeyboardMoveAnimator;
    private ObjectAnimator mKeyboardAlphaAnimator;

    private ObjectAnimator mTipsMoveAnimator;
    private ObjectAnimator mTipsAlphaAnimator;

    private Animation mAnimation;

    private boolean mForbidMove = false;
    private boolean mFocusTips = false;
    private boolean mCanSaveHistory = false;

    private ImageView mLoadingImage;
    private Button mRefreshButton;

    private int mHotSearchPageNumber = 1;
    private int mSearchResultPageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        initData();
        initView();
        initListener();
        initObserve();
        mViewModel.getSearchHistory();
    }

    private void initData() {
        mViewModel = new ViewModelProvider(this, new SearchViewModelFactory(
                SearchRepository.getInstance())).get(SearchViewModel.class);

        mSearchResultPresenter = new SearchResultPresenter();
        mArrayObjectAdapter = new ArrayObjectAdapter(mSearchResultPresenter);
        mItemBridgeAdapter = new ItemBridgeAdapter(mArrayObjectAdapter);

        mSearchTipsAdapter = new SearchTipsAdapter(DimensionUtils.getSizeFromDP(getResources(), R.dimen.src_dp_40));

        ViewGroup.MarginLayoutParams keyboardParams = (ViewGroup.MarginLayoutParams) mBinding.kbSearchKeyboard.getLayoutParams();
        int keyboardMoveDistance = keyboardParams.width + keyboardParams.leftMargin;

        ViewGroup.MarginLayoutParams tipsParams = (ViewGroup.MarginLayoutParams) mBinding.rvSearchTips.getLayoutParams();
        int tipsMoveDistance = tipsParams.width + tipsParams.leftMargin + keyboardMoveDistance;

        mKeyboardMoveAnimator = ObjectAnimator.ofInt(mBinding.llSearchRoot, "scrollX",
                0, keyboardMoveDistance).setDuration(Constant.COMMON_DURATION);
        mKeyboardMoveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mKeyboardAlphaAnimator = ObjectAnimator.ofFloat(mBinding.kbSearchKeyboard, "alpha",
                1, 0).setDuration(Constant.COMMON_DURATION);
        mKeyboardAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mTipsMoveAnimator = ObjectAnimator.ofInt(mBinding.llSearchRoot, "scrollX",
                keyboardMoveDistance, tipsMoveDistance).setDuration(Constant.COMMON_DURATION);
        mTipsMoveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mTipsAlphaAnimator = ObjectAnimator.ofFloat(mBinding.rvSearchTips, "alpha",
                1, 0).setDuration(Constant.COMMON_DURATION);
        mTipsAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading_anim);
        mAnimation.setInterpolator(new LinearInterpolator());
    }

    private void initView() {
        mBinding.vgvSearchCourse.setNumColumns(Constant.SEARCH_RESULT_COLUMN_NUM);
        mBinding.vgvSearchCourse.setVerticalSpacing(DimensionUtils.getSizeFromDP(getResources(), R.dimen.src_dp_18));
        mBinding.vgvSearchCourse.setAdapter(mItemBridgeAdapter);

        FocusHighlightHelper.setupBrowseItemFocusHighlight(mItemBridgeAdapter,
                FocusHighlight.ZOOM_FACTOR_LARGE, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBinding.rvSearchTips.setLayoutManager(layoutManager);
        mBinding.rvSearchTips.setAdapter(mSearchTipsAdapter);

        mBinding.rvSearchTips.setCurrentFocusPosition(1);
        mBinding.vgvSearchCourse.setFocusable(false);

        mLoadingImage = mBinding.includeSearchLoading.findViewById(R.id.iv_loading_dp);
        mRefreshButton = mBinding.includeSearchFailed.findViewById(R.id.btn_req_failed_base);
    }

    private void initListener() {
        mBinding.kbSearchKeyboard.setOnMyTextChangedListener(text -> {
            if (mBinding.includeSearchEmpty.getVisibility() == View.VISIBLE && text.isEmpty()) {
                mBinding.includeSearchEmpty.setVisibility(View.GONE);
                mBinding.rvSearchTips.setVisibility(View.VISIBLE);
                mBinding.llSearchCourse.setVisibility(View.VISIBLE);
            }
            if (text.isEmpty()) {
                mArrayObjectAdapter.clear();
                mArrayObjectAdapter.addAll(0, mViewModel.pHotSearchResult);
                mSearchTipsAdapter.setShowView(SearchTipsAdapter.SHOW_TIPS);
                mBinding.rvSearchTips.setCurrentFocusPosition(1);
            } else {
                mSearchTipsAdapter.setSearchInput(text);
                mBinding.rvSearchTips.setCurrentFocusPosition(2);
                mSearchResultPageNumber = 1;
                mViewModel.reqSearchResult(text, mSearchResultPageNumber);
            }
            mCanSaveHistory = false;
        });

        mBinding.kbSearchKeyboard.setFocusLostListener((lastFocusChild, direction) -> {
            if (direction == View.FOCUS_RIGHT && mBinding.rvSearchTips.getVisibility() == View.VISIBLE) {
                mBinding.rvSearchTips.requestFocus();
                mFocusTips = true;
                mForbidMove = true;
                mKeyboardMoveAnimator.start();
                mKeyboardAlphaAnimator.start();
                mBinding.vgvSearchCourse.setFocusable(true);
            }
        });

        mBinding.rvSearchTips.setFocusLostListener((lastFocusChild, direction) -> {
            if (direction == View.FOCUS_RIGHT) {
                mForbidMove = true;
                mTipsMoveAnimator.start();
                mTipsAlphaAnimator.start();
                mBinding.vgvSearchCourse.requestFocus();
                mFocusTips = false;
            }
            if (direction == View.FOCUS_LEFT) {
                mBinding.kbSearchKeyboard.requestFocus();
                mFocusTips = false;
                mBinding.vgvSearchCourse.setFocusable(false);
                mForbidMove = true;
                mKeyboardMoveAnimator.reverse();
                mKeyboardAlphaAnimator.reverse();
            }
        });

        mKeyboardMoveAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean reverse) {
                super.onAnimationEnd(animation);
                mForbidMove = false;
                if (mCanSaveHistory && reverse && mBinding.kbSearchKeyboard.getInputText().isEmpty()) {
                    mArrayObjectAdapter.clear();
                    mArrayObjectAdapter.addAll(0, mViewModel.pHotSearchResult);
                    mCanSaveHistory = false;
                }
            }
        });

        mTipsMoveAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mForbidMove = false;
            }
        });

        mBinding.vgvSearchCourse.setOnFocusLostListener(direction -> {
            if (direction == View.FOCUS_LEFT) {
                mForbidMove = true;
                mTipsMoveAnimator.reverse();
                mTipsAlphaAnimator.reverse();
                mBinding.rvSearchTips.requestFocus();
                mFocusTips = true;
            }
        });

        mBinding.vgvSearchCourse.setOnChildSelectedListener((parent, view, position, id) -> {
            if (position >= Constant.SEARCH_RESULT_COLUMN_NUM) {
                if (mBinding.tvSearchCourseTitle.getVisibility() == View.VISIBLE) {
                    mBinding.tvSearchCourseTitle.setVisibility(View.GONE);
                }
            } else {
                if (mBinding.tvSearchCourseTitle.getVisibility() == View.GONE) {
                    mBinding.tvSearchCourseTitle.setVisibility(View.VISIBLE);
                }
            }
            if (!mBinding.kbSearchKeyboard.getInputText().isEmpty()
                    && mArrayObjectAdapter.size() == mSearchResultPageNumber * Constant.SEARCH_RESULT_PAGE_SIZE
                    && position / Constant.SEARCH_RESULT_COLUMN_NUM == (mArrayObjectAdapter.size() - 1) / Constant.SEARCH_RESULT_COLUMN_NUM) {
                mViewModel.reqSearchResult(mBinding.kbSearchKeyboard.getInputText(), ++mSearchResultPageNumber);
            }
        });

        mItemBridgeAdapter.setAdapterListener(new ItemBridgeAdapter.AdapterListener() {
            @Override
            public void onCreate(ItemBridgeAdapter.ViewHolder viewHolder) {
                viewHolder.itemView.setOnClickListener(v -> {
                    int position = viewHolder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    Object item = mArrayObjectAdapter.get(position);
                    if (item instanceof CourseInfo) {
                        CourseInfo courseInfo = (CourseInfo) item;
                        try {
                            Intent intent = new Intent(SearchActivity.this,
                                    Class.forName("com.zwn.launcher.ui.detail.DetailActivity"));
                            intent.putExtra("skuId", courseInfo.getSkuId());
                            startActivity(intent);
                            if (mCanSaveHistory) mViewModel.saveSearchHistory(courseInfo.getName());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        mSearchTipsAdapter.setOnItemClickListener((v, position, name) -> {
            if (name.equals(SearchTipsAdapter.DELETE_NAME)) {
                mSearchTipsAdapter.clearSearchHistory(mViewModel.getHistoryNum());
                mViewModel.clearSearchHistory();
            } else {
                mSearchResultPageNumber = 1;
                mViewModel.reqSearchResult(name, mSearchResultPageNumber);
            }
        });

        mSearchTipsAdapter.setOnItemFocusListener(position -> {
            if (mSearchTipsAdapter.getShowView() == SearchTipsAdapter.SHOW_TIPS
                    && position == mSearchTipsAdapter.getItemCount() - 1
                    && mViewModel.pHotSearchResult.size() == mHotSearchPageNumber * Constant.HOT_SEARCH_PAGE_SIZE) {
                mViewModel.reqHotSearchList(++mHotSearchPageNumber);
            }
        });

        mRefreshButton.setOnClickListener(v -> {
            mBinding.includeSearchFailed.setVisibility(View.GONE);
            mHotSearchPageNumber = 1;
            mViewModel.reqHotSearchList(mHotSearchPageNumber);
        });
    }

    private void initObserve() {
        mViewModel.pSearchHistory.observe(this, state -> {
            if (state == LoadState.Success) {
                mSearchTipsAdapter.addTipsItem(new CommonSearchModel(
                        SearchTipsAdapter.TYPE_TITLE_WITH_DELETE, "搜索历史"));
                for (int i = 0; i < mViewModel.getHistoryNum(); i++) {
                    mSearchTipsAdapter.addTipsItem(new CommonSearchModel(
                            SearchTipsAdapter.TYPE_PRODUCT_NAME, mViewModel.getHistoryItem(i)));
                }
            }
            mViewModel.reqHotSearchList(mHotSearchPageNumber);
        });

        mViewModel.pHotSearchState.observe(this, state -> {
            if (mHotSearchPageNumber == 1) {
                switch (state) {
                    case Loading:
                        mBinding.includeSearchLoading.setVisibility(View.VISIBLE);
                        mLoadingImage.setAnimation(mAnimation);
                        break;
                    case Success:
                        mLoadingImage.clearAnimation();
                        mBinding.includeSearchLoading.setVisibility(View.GONE);
                        mBinding.kbSearchKeyboard.setVisibility(View.VISIBLE);
                        mBinding.rvSearchTips.setVisibility(View.VISIBLE);
                        mBinding.llSearchCourse.setVisibility(View.VISIBLE);

                        mSearchTipsAdapter.addTipsItem(new CommonSearchModel(SearchTipsAdapter.TYPE_TITLE_WITHOUT_DELETE, "热门搜索"));
                        for (CourseInfo courseInfo: mViewModel.pHotSearchResult) {
                            mSearchTipsAdapter.addTipsItem(new CommonSearchModel(
                                    SearchTipsAdapter.TYPE_PRODUCT_NAME,
                                    courseInfo.getName()));
                        }
                        mArrayObjectAdapter.addAll(0, mViewModel.pHotSearchResult);
                        mSearchTipsAdapter.notifyDataSetChanged();
                        break;
                    case Failed:
                        mLoadingImage.clearAnimation();
                        mBinding.includeSearchLoading.setVisibility(View.GONE);
                        mBinding.includeSearchFailed.setVisibility(View.VISIBLE);
                        mRefreshButton.requestFocus();
                        break;
                }
                return;
            }
            switch (state) {
                case Loading:
                    break;
                case Success:
                    assert (mSearchTipsAdapter.getShowView() == SearchTipsAdapter.SHOW_TIPS);
                    int startChange = mSearchTipsAdapter.getItemCount();
                    int changeCount = 0;
                    for (int i = startChange - 1; i < mViewModel.pHotSearchResult.size(); i++) {
                        mSearchTipsAdapter.addTipsItem(new CommonSearchModel(
                                SearchTipsAdapter.TYPE_PRODUCT_NAME,
                                mViewModel.pHotSearchResult.get(i).getName()));
                        mArrayObjectAdapter.add(mViewModel.pHotSearchResult.get(i));
                        changeCount++;
                    }
                    mSearchTipsAdapter.notifyItemRangeChanged(startChange, changeCount);
                    break;
                case Failed:
                    mHotSearchPageNumber--;
                    Toast.makeText(this, "热门搜索请求失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        mViewModel.pSearchState.observe(this, state -> {
            if (mSearchResultPageNumber == 1) {
                switch (state) {
                    case Loading:
                        break;
                    case Success:
                        mArrayObjectAdapter.clear();
                        mSearchTipsAdapter.clearRelative();
                        boolean hasResult = !mViewModel.pSearchInfo.isEmpty();
                        if (mFocusTips) {
                            if (hasResult) {
                                mArrayObjectAdapter.addAll(0, mViewModel.pSearchInfo);
                                mForbidMove = true;
                                mTipsMoveAnimator.start();
                                mTipsAlphaAnimator.start();
                                mBinding.vgvSearchCourse.requestFocus();
                                mCanSaveHistory = true;
                            } else {
                                mBinding.llSearchRoot.scrollTo(0, 0);
                                mBinding.kbSearchKeyboard.setAlpha(1);
                                mBinding.kbSearchKeyboard.requestFocus();
                                mCanSaveHistory = false;
                            }
                            mFocusTips = false;
                            return;
                        }
                        if (hasResult) {
                            if (mBinding.includeSearchEmpty.getVisibility() == View.VISIBLE) {
                                mBinding.includeSearchEmpty.setVisibility(View.GONE);
                                mBinding.rvSearchTips.setVisibility(View.VISIBLE);
                                mBinding.llSearchCourse.setVisibility(View.VISIBLE);
                            }
                            for (CourseInfo courseInfo: mViewModel.pSearchInfo) {
                                mSearchTipsAdapter.addRelativeItem(new CommonSearchModel(SearchTipsAdapter.TYPE_PRODUCT_NAME, courseInfo.getName()));
                            }
                            mSearchTipsAdapter.setShowView(SearchTipsAdapter.SHOW_RELATIVE);
                            mArrayObjectAdapter.addAll(0, mViewModel.pSearchInfo);
                        } else if (mBinding.includeSearchEmpty.getVisibility() == View.GONE) {
                            mBinding.includeSearchEmpty.setVisibility(View.VISIBLE);
                            mBinding.rvSearchTips.setVisibility(View.GONE);
                            mBinding.llSearchCourse.setVisibility(View.GONE);
                        }
                        break;
                    case Failed:
                        Toast.makeText(this, "搜索请求失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                        break;
                }
                return;
            }
            switch (state) {
                case Loading:
                    break;
                case Success:
                    for (int i = mArrayObjectAdapter.size(); i < mViewModel.pSearchInfo.size(); i++) {
                        mArrayObjectAdapter.add(mViewModel.pSearchInfo.get(i));
                    }
                    break;
                case Failed:
                    mSearchResultPageNumber--;
                    Toast.makeText(this, "搜索请求失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mForbidMove || super.onKeyDown(keyCode, event);
    }
}