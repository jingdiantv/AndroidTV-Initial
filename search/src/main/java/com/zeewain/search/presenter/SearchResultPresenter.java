package com.zeewain.search.presenter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.search.R;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.search.data.model.CourseInfo;

public class SearchResultPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof CourseInfo) {
            ViewHolder vh = (ViewHolder) viewHolder;
            CourseInfo courseInfo = (CourseInfo) item;
            if (courseInfo.getImageUrl() == null || courseInfo.getImageUrl().isEmpty()) {
                return;
            }
            GlideApp.with(vh.img.getContext())
                    .load(courseInfo.getImageUrl())
                    .error(R.drawable.list_favor_iv_error)
                    .apply(CommonVariableCacheUtils.getInstance().getOptions13())
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(vh.img);
            vh.title.setText(courseInfo.getName());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public static class ViewHolder extends Presenter.ViewHolder {
        public ImageView img;
        public TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_search_result);
            title = itemView.findViewById(R.id.tv_search_result);
        }
    }
}

