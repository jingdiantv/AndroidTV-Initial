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
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.model.ProductListMo;


public class TypeMainSlavePresenter extends Presenter {
    private Context mContext;

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_type_main_slave_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof ProductListMo.Record) {
            ProductListMo.Record record = (ProductListMo.Record) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            if(ProdConstants.LATEST_INTERACT_ITEM.equals(record.getSpuId())){
                vh.mIvTypeMainSlave.setImageResource(R.mipmap.latest_interact);
            }else {
                GlideApp.with(mContext)
                        .load(record.getProductImg())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.bg_shape_default))
                        .into(vh.mIvTypeMainSlave);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final ImageView mIvTypeMainSlave;

        public ViewHolder(View view) {
            super(view);
            mIvTypeMainSlave = view.findViewById(R.id.iv_type_main_slave);
        }
    }
}
