package com.zwn.launcher.presenter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.request.RequestOptions;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.widgets.LoadingView;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.widgets.ScanningConstraintLayout;


public class TypePagedRecommendPresenter extends Presenter {

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_recommend_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof ProductListMo.Record) {
            ProductListMo.Record record = (ProductListMo.Record) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            if(ProdConstants.SKU_ID_LOADING.equals(record.getSkuId())) {
                vh.showLoading();
                vh.mTvTypeRecommendTitle.setText("");
            }else if(ProdConstants.SKU_ID_LOADED_ERR.equals(record.getSkuId())){
                vh.showLoadedErr();
                vh.mTvTypeRecommendTitle.setText("");
            }else{
                vh.showLoadedDone();
                GlideApp.with(vh.view.getContext())
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
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public static class ViewHolder extends Presenter.ViewHolder implements View.OnFocusChangeListener{

        private final TextView mTvTypeRecommendTitle;
        private final ImageView mIvTypeRecommend;
        private final CardView cardViewRecommend;
        private final LoadingView loadingView;
        private final LinearLayout networkErrView;
        private final ScanningConstraintLayout lightLayoutTypeRecommend;

        public ViewHolder(View view) {
            super(view);
            mTvTypeRecommendTitle = view.findViewById(R.id.tv_type_recommend_title);
            mIvTypeRecommend = view.findViewById(R.id.iv_type_recommend);
            cardViewRecommend = view.findViewById(R.id.cardView_type_recommend);
            loadingView = view.findViewById(R.id.loadingView_type_recommend);
            networkErrView = view.findViewById(R.id.ll_netErr_type_recommend);
            lightLayoutTypeRecommend = view.findViewById(R.id.lightLayout_type_recommend);
            view.setOnFocusChangeListener(this);
        }

        public void showLoading(){
            loadingView.setVisibility(View.VISIBLE);
            loadingView.startAnim();
            networkErrView.setVisibility(View.GONE);
            cardViewRecommend.setVisibility(View.GONE);
        }

        public void showLoadedErr(){
            loadingView.setVisibility(View.GONE);
            loadingView.stopAnim();
            networkErrView.setVisibility(View.VISIBLE);
            cardViewRecommend.setVisibility(View.GONE);
        }

        public void showLoadedDone(){
            loadingView.setVisibility(View.GONE);
            loadingView.stopAnim();
            networkErrView.setVisibility(View.GONE);
            cardViewRecommend.setVisibility(View.VISIBLE);
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
