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
import com.zeewain.base.utils.AutoUtils;
import com.zeewain.base.utils.GlideApp;
import com.zwn.user.R;
import com.zwn.user.data.model.MessageCenterItem;

import java.util.List;

public class MessageCenterAdapter extends BaseAdapter {
    private List<MessageCenterItem> itemList;
    private int mImgRadius;
    private boolean mOnDelete = false;

    public MessageCenterAdapter(List<MessageCenterItem> itemList, int imgRadius) {
        this.itemList = itemList;
        this.mImgRadius = imgRadius;
    }

    public void isOnDelete(boolean onDelete) {
        mOnDelete = onDelete;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mine_msg_item, parent, false);
            AutoUtils.auto(convertView);
            viewHolder.msgImg = convertView.findViewById(R.id.iv_mine_msg_item);
            viewHolder.title = convertView.findViewById(R.id.tv_mine_msg_item_title);
            viewHolder.introduction = convertView.findViewById(R.id.tv_mine_msg_item_intro);
            viewHolder.date = convertView.findViewById(R.id.tv_mine_msg_item_date);
            viewHolder.delText = convertView.findViewById(R.id.tv_mine_msg_item_del);
            viewHolder.viewGroup = convertView.findViewById(R.id.cl_mine_msg_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        RequestOptions options = new RequestOptions()
                .transform(new MultiTransformation(
                        new CenterCrop(),
                        new RoundedCorners(mImgRadius)));
        GlideApp.with(viewHolder.msgImg.getContext())
                .load(itemList.get(position).url)
                .error(R.drawable.mine_list_favor_iv_error)
                .apply(options)
                .into(viewHolder.msgImg);
        viewHolder.title.setText(itemList.get(position).title);
        viewHolder.introduction.setText(itemList.get(position).introduction);
        viewHolder.date.setText(itemList.get(position).date);
        viewHolder.delText.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    private static class ViewHolder {
        public ImageView msgImg;
        public TextView title;
        public TextView introduction;
        public TextView date;
        public TextView delText;
        public ConstraintLayout viewGroup;
    }
}
