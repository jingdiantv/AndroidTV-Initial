package com.zeewain.search.adapter;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.search.R;
import com.zeewain.search.model.CommonSearchModel;
import com.zeewain.search.viewholder.BaseViewHolder;
import com.zeewain.search.viewholder.TipsItemViewHolder;
import com.zeewain.search.viewholder.TipsTipsViewHolder;
import com.zeewain.search.viewholder.TipsTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SearchTipsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SearchTipsAdapter";

    public static final int TYPE_TITLE_WITHOUT_DELETE = 0;
    public static final int TYPE_TITLE_WITH_DELETE = 1;
    public static final int TYPE_RESULT_TIPS = 2;
    public static final int TYPE_PRODUCT_NAME = 3;
    public static final String DELETE_NAME = "DELETE_NAME";
    public static final int SHOW_TIPS = 100;
    public static final int SHOW_RELATIVE = 101;

    private static final String HISTORY_TIPS_END = "的全部结果";

    private int mShowView = SHOW_TIPS;

    private final int mMarginTop;
    private final List<CommonSearchModel> mSearchTipsList = new ArrayList<>();
    private final List<CommonSearchModel> mSearchRelativeList = new ArrayList<>();
    
    private OnItemClickListener mOnItemClickListener;
    private OnItemFocusListener mOnItemFocusListener;

    public SearchTipsAdapter(int marginTop) {
        mMarginTop = marginTop;

        mSearchRelativeList.add(new CommonSearchModel(SearchTipsAdapter.TYPE_TITLE_WITHOUT_DELETE, "猜你想搜"));
        mSearchRelativeList.add(new CommonSearchModel(SearchTipsAdapter.TYPE_RESULT_TIPS, ""));
    }

    public void addTipsItem(CommonSearchModel model) {
        mSearchTipsList.add(model);
    }

    public void addRelativeItem(CommonSearchModel model) {
        mSearchRelativeList.add(model);
    }

    public void clearRelative() {
        if (mSearchRelativeList.size() > 2) {
            mSearchRelativeList.subList(2, mSearchRelativeList.size()).clear();
        }
    }

    public void clearSearchHistory(int historyNum) {
        mSearchTipsList.subList(0, historyNum + 1).clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mShowView == SHOW_TIPS) {
            switch (viewType) {
                case TYPE_TITLE_WITHOUT_DELETE:
                case TYPE_TITLE_WITH_DELETE:
                    return new TipsTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_search_tips_title, parent, false));
                case TYPE_PRODUCT_NAME:
                    return new TipsItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_search_tips, parent, false));
            }
        }
        if (mShowView == SHOW_RELATIVE) {
            switch (viewType) {
                case TYPE_TITLE_WITHOUT_DELETE:
                    return new TipsTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_search_tips_title, parent, false));
                case TYPE_RESULT_TIPS:
                    return new TipsTipsViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_search_tips_tips, parent, false));
                case TYPE_PRODUCT_NAME:
                    return new TipsItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_search_tips, parent, false));
            }
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowView == SHOW_TIPS) {
            return mSearchTipsList.get(position).getViewType();
        }
        if (mShowView == SHOW_RELATIVE) {
            return mSearchRelativeList.get(position).getViewType();
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
        int viewType = TYPE_TITLE_WITHOUT_DELETE;
        if (mShowView == SHOW_TIPS) {
            baseViewHolder.bindHolder(mSearchTipsList.get(position));
            viewType = mSearchTipsList.get(position).getViewType();
            if (position != 0 && (viewType == TYPE_TITLE_WITHOUT_DELETE || viewType == TYPE_TITLE_WITH_DELETE)) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) baseViewHolder.itemView.getLayoutParams();
                layoutParams.setMargins(0, mMarginTop, 0, 0);
            }
        }
        if (mShowView == SHOW_RELATIVE) {
            baseViewHolder.bindHolder(mSearchRelativeList.get(position));
            viewType = mSearchRelativeList.get(position).getViewType();
        }
        if (position == 0) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) baseViewHolder.itemView.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
        }

        baseViewHolder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mOnItemFocusListener.onItemFocus(position);
            }
        });
        switch (viewType) {
            case TYPE_TITLE_WITH_DELETE:
                TipsTitleViewHolder tipsTitleViewHolder = (TipsTitleViewHolder) baseViewHolder;
                tipsTitleViewHolder.imageView.setOnClickListener(v -> {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(holder.itemView, position, DELETE_NAME);
                    }
                });
                break;
            case TYPE_PRODUCT_NAME:
                TipsItemViewHolder tipsItemViewHolder = (TipsItemViewHolder) baseViewHolder;
                tipsItemViewHolder.textView.setOnClickListener(v -> {
                    if (mOnItemClickListener != null) {
                        String name = tipsItemViewHolder.textView.getText().toString();
                        mOnItemClickListener.onItemClick(holder.itemView, position, name);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mShowView == SHOW_TIPS) {
            return mSearchTipsList.size();
        }
        if (mShowView == SHOW_RELATIVE) {
            return mSearchRelativeList.size();
        }
        return 0;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position, String name);
    }

    public interface OnItemFocusListener {
        void onItemFocus(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemFocusListener(OnItemFocusListener onItemFocusListener) {
        mOnItemFocusListener = onItemFocusListener;
    }

    public void setShowView(int showView) {
        mShowView = showView;
        notifyDataSetChanged();
    }

    public int getShowView() {
        return mShowView;
    }

    public void setSearchInput(String input) {
        if (input.length() > 5) {
            input = input.substring(0, 5) + "...";
        }
        input = "&#8220" + input + "&#8221 ";
        String newTips = input + HISTORY_TIPS_END;
        mSearchRelativeList.get(1).setViewName(newTips);
    }
}