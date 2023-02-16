package com.zwn.launcher.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import com.zwn.launcher.R;
import com.zwn.launcher.data.model.MainCategoryMo;


public class MainCategoryPresenter extends Presenter {

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof MainCategoryMo) {
            ViewHolder vh = (ViewHolder) viewHolder;
            vh.mTvMainTitle.setText(((MainCategoryMo) item).getCategoryName());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final TextView mTvMainTitle;

        ViewHolder(View view) {
            super(view);
            mTvMainTitle = view.findViewById(R.id.tv_main_title);
        }
    }
}
