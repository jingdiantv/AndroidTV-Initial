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
import com.zeewain.base.utils.GlideApp;
import com.zwn.user.R;
import com.zwn.user.data.model.HistoryItem;

public class HistoryPresenter extends Presenter {
    private final RequestOptions mOptions;
    private boolean mOnDelete = false;

    public HistoryPresenter(int imageRadius) {
        mOptions = new RequestOptions()
                .transform(new MultiTransformation(
                        new CenterCrop(),
                        new RoundedCorners(imageRadius)
                ));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.mine_hist_item, parent, false);
        return new ViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof HistoryItem) {
            ViewHolder vh = (ViewHolder) viewHolder;
            HistoryItem historyItem = (HistoryItem) item;
            GlideApp.with(vh.img.getContext())
                    .load(historyItem.url)
                    .error(R.drawable.mine_list_favor_iv_error)
                    .apply(mOptions)
                    .into(vh.img);
            vh.title.setText(historyItem.title);
            vh.delText.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public void isOnDelete(boolean onDelete) {
        mOnDelete = onDelete;
    }

    public static class ViewHolder extends Presenter.ViewHolder {
        public ImageView img;
        public TextView title;
        public TextView delText;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_mine_hist_item);
            title = itemView.findViewById(R.id.tv_mine_hist_item_title);
            delText = itemView.findViewById(R.id.tv_mine_hist_item_del);
        }
    }
}
