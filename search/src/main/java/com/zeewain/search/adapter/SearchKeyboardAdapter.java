package com.zeewain.search.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.search.R;
import com.zeewain.search.model.CommonSearchModel;
import com.zeewain.search.viewholder.BaseViewHolder;
import com.zeewain.search.viewholder.BoardFuncViewHolder;
import com.zeewain.search.viewholder.BoardInputViewHolder;
import com.zeewain.search.viewholder.BoardKeyViewHolder;

public class SearchKeyboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_INPUT = 0;
    public static final int TYPE_KEY = 1;
    public static final int TYPE_FUNC = 2;

    private boolean mInitFlag = true;
    public final CommonSearchModel[] mItemList;
    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;
    private OnItemFocusListener onItemFocusListener;

    public SearchKeyboardAdapter(CommonSearchModel[] itemList) {
        mItemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_INPUT:
                return new BoardInputViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.item_keyboard_input, viewGroup, false));
            case TYPE_KEY:
                return new BoardKeyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.item_keyboard_key, viewGroup, false));
            case TYPE_FUNC:
                return new BoardFuncViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.item_keyboard_func, viewGroup, false));
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return mItemList[position].getViewType();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
        baseViewHolder.bindHolder(mItemList[position]);

        if (mInitFlag && position == 0) {
            mInitFlag = false;
            baseViewHolder.itemView.requestFocus();
        }

        baseViewHolder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (onItemFocusListener != null) {
                    onItemFocusListener.onItemFocusSection(position, mItemList[position].getViewName());
                }
            }
        });

        baseViewHolder.itemView.setOnClickListener(v -> {
            if (onRecyclerViewItemClickListener != null) {
                onRecyclerViewItemClickListener.onItemClick(position, mItemList[position].getViewName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItemList.length;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.onRecyclerViewItemClickListener = listener;
    }

    public void setOnItemFocusListener(OnItemFocusListener onItemFocusListener) {
        this.onItemFocusListener = onItemFocusListener;
    }

    public interface OnItemFocusListener {
        void onItemFocusSection(int position, String data);
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(int index, String data);
    }
}
