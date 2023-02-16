package com.zeewain.search.viewholder;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.search.model.CommonSearchModel;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public View itemView;

    public abstract void bindHolder(CommonSearchModel model);
}
