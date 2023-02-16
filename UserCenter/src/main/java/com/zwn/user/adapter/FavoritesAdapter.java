package com.zwn.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zeewain.base.utils.GlideApp;
import com.zwn.user.R;
import com.zwn.user.data.model.FavoritesItem;

import java.util.List;

public class FavoritesAdapter extends BaseAdapter {

    private final List<FavoritesItem> mItemList;
    private boolean mOnDelete;
    private final int mImgRadius;

    public FavoritesAdapter(List<FavoritesItem> itemList, int imgRadius) {
        mItemList = itemList;
        mImgRadius = imgRadius;
    }

    public void isOnDelete(boolean onDelete) {
        this.mOnDelete = onDelete;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.mine_list_item, parent, false);
            // AutoUtils.auto(convertView);
            viewHolder.productImage = convertView.findViewById(R.id.iv_mine_list_item);
            viewHolder.title = convertView.findViewById(R.id.tv_mine_list_item_title);
            viewHolder.introduction = convertView.findViewById(R.id.tv_mine_list_item_intro);
            viewHolder.deleteText = convertView.findViewById(R.id.tv_mine_list_item_del);
            viewHolder.constraintLayout = convertView.findViewById(R.id.cl_mine_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        FavoritesItem favoritesItem = mItemList.get(position);
        RequestOptions options = new RequestOptions()
                .transform(new MultiTransformation(
                        new CenterCrop(),
                        new RoundedCorners(mImgRadius)));
        GlideApp.with(viewHolder.productImage.getContext())
                .load(favoritesItem.url)
                .error(R.drawable.mine_list_favor_iv_error)
                .apply(options)
                .into(viewHolder.productImage);
        viewHolder.title.setText(favoritesItem.title);
        viewHolder.introduction.setText(favoritesItem.summary);
        viewHolder.deleteText.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    private static final class ViewHolder {
        ImageView productImage;
        TextView title;
        TextView introduction;
        TextView deleteText;
        ConstraintLayout constraintLayout;
    }
}
