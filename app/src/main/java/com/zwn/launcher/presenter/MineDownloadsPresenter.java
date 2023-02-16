package com.zwn.launcher.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.zeewain.base.model.MineCommonMo;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;

public class MineDownloadsPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_imageview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof MineCommonMo) {
            MineCommonMo model = (MineCommonMo) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            if (model.type.equals(ProdConstants.MY_DOWNLOADS)) {
                vh.imageView.setBackgroundResource(R.mipmap.ic_all_download);
            } else {
                Glide.with(vh.imageView.getContext())
                        .load(model.imgUrl)
                        .placeholder(CommonVariableCacheUtils.getInstance().getLoadingDrawable26())
                        .apply(CommonVariableCacheUtils.getInstance().getOptions13())
                        .error(CommonVariableCacheUtils.getInstance().getLoadFailedDrawable26())
                        .into(vh.imageView);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    private static class ViewHolder extends Presenter.ViewHolder {
        public ImageView imageView;

        public ViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.iv_item_mine_imageview);
        }
    }
}
