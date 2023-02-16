package com.zwn.user.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zeewain.base.utils.GlideApp;
import com.zwn.user.R;
import com.zwn.user.data.model.FavoritesItem;

public class FavoritesPresenter extends Presenter {

    private final RequestOptions mOptions;
    private boolean mOnDelete = false;

    public FavoritesPresenter(int imgRadius) {
        mOptions = new RequestOptions()
                .transform(new MultiTransformation<>(
                        new CenterCrop(),
                        new RoundedCorners(imgRadius)
                ));
    }

    public void isOnDelete(boolean onDelete) {
        mOnDelete = onDelete;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mine_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof FavoritesItem) {
            FavoritesItem favoritesItem = (FavoritesItem) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            GlideApp.with(vh.productImage.getContext())
                    .load(favoritesItem.url)
                    .error(R.drawable.mine_list_favor_iv_error)
                    .apply(mOptions)
                    .into(vh.productImage);
            vh.title.setText(favoritesItem.title);
            vh.introduction.setText(favoritesItem.summary);
            vh.deleteText.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    private static class ViewHolder extends Presenter.ViewHolder {
        public ImageView productImage;
        public TextView title;
        public TextView introduction;
        public TextView deleteText;
        public ConstraintLayout constraintLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.iv_mine_list_item);
            title = itemView.findViewById(R.id.tv_mine_list_item_title);
            introduction = itemView.findViewById(R.id.tv_mine_list_item_intro);
            deleteText = itemView.findViewById(R.id.tv_mine_list_item_del);
            constraintLayout = itemView.findViewById(R.id.cl_mine_list_item);
        }
    }
}
