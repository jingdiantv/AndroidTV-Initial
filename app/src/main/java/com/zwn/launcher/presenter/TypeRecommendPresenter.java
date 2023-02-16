package com.zwn.launcher.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import com.bumptech.glide.request.RequestOptions;
import com.zeewain.base.utils.GlideApp;
import com.zwn.launcher.R;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.widgets.ScanningConstraintLayout;


public class TypeRecommendPresenter extends Presenter {
    private Context mContext;

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_type_recommend_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof ProductListMo.Record) {
            ProductListMo.Record record = (ProductListMo.Record) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            GlideApp.with(mContext)
                    .load(record.getProductImg())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.bg_shape_default))
                    .into(vh.mIvTypeRecommend);
            String title = record.getProductTitle();
            if (!TextUtils.isEmpty(title)) {
                vh.mTvTypeRecommendTitle.setText(title);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public static class ViewHolder extends Presenter.ViewHolder implements View.OnFocusChangeListener{

        private final TextView mTvTypeRecommendTitle;
        private final ImageView mIvTypeRecommend;
        private final ScanningConstraintLayout lightLayoutTypeRecommend;

        public ViewHolder(View view) {
            super(view);
            mTvTypeRecommendTitle = view.findViewById(R.id.tv_type_recommend_title);
            mIvTypeRecommend = view.findViewById(R.id.iv_type_recommend);
            lightLayoutTypeRecommend = view.findViewById(R.id.lightLayout_type_recommend);
            view.setOnFocusChangeListener(this);
        }

        @Override
        public void onFocusChange(View view, boolean b) {
            if(b){
                lightLayoutTypeRecommend.startAnimator();
            }else{
                lightLayoutTypeRecommend.stopAnimator();
            }
        }
    }

}
