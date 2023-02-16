package com.zwn.launcher.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.leanback.widget.Presenter;

import com.zwn.launcher.R;
import com.zeewain.base.model.MineCommonMo;

public class MineFooterPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_footer, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof MineCommonMo) {
            MineCommonMo model = (MineCommonMo) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            vh.imageView.setImageResource(model.itemId);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    private static class ViewHolder extends Presenter.ViewHolder {
        public ImageView imageView;

        public ViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.iv_mine_footer);
        }
    }
}
