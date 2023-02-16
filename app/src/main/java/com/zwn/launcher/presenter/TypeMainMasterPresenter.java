package com.zwn.launcher.presenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.leanback.widget.Presenter;


import com.bumptech.glide.request.RequestOptions;

import com.zeewain.base.utils.GlideApp;
import com.zwn.launcher.R;
import com.zwn.launcher.data.model.ProductListMo;


public class TypeMainMasterPresenter extends Presenter {
    private Context mContext;

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_type_main_master_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof ProductListMo.Record) {
            ViewHolder vh = (ViewHolder) viewHolder;
            GlideApp.with(mContext)
                    .load(((ProductListMo.Record) item).getProductImg())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.bg_shape_default))
                    .into(vh.mIvTypeMain);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final ImageView mIvTypeMain;

        public ViewHolder(View view) {
            super(view);
            mIvTypeMain = view.findViewById(R.id.iv_type_main);
        }
    }
}
