package com.zwn.user.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zeewain.base.model.MineCommonMo;
import com.zeewain.base.utils.CommonVariableCacheUtils;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.R;

public class DownloadsPresenter extends Presenter {

    private boolean mOnDelete = false;

    public void isOnDelete(boolean onDelete) {
        mOnDelete = onDelete;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mine_down_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof DownloadInfo) {
            DownloadInfo downloadInfo = (DownloadInfo) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            vh.title.setText(downloadInfo.fileName);
            vh.delete.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);
            Glide.with(vh.image.getContext())
                    .load(downloadInfo.fileImgUrl)
                    .placeholder(CommonVariableCacheUtils.getInstance().getLoadingDrawable26())
                    .apply(CommonVariableCacheUtils.getInstance().getOptions13())
                    .placeholder(CommonVariableCacheUtils.getInstance().getLoadFailedDrawable26())
                    .into(vh.image);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    private static class ViewHolder extends Presenter.ViewHolder {
        public final ImageView image;
        public final TextView title;
        public final TextView delete;

        public ViewHolder(View view) {
            super(view);

            image = view.findViewById(R.id.iv_mine_down_item);
            title = view.findViewById(R.id.tv_mine_down_item_title);
            delete = view.findViewById(R.id.tv_mine_down_item_del);
        }
    }
}
