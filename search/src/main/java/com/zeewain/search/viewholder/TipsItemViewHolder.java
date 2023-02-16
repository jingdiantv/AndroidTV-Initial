package com.zeewain.search.viewholder;

import android.view.View;
import android.widget.TextView;

import com.example.search.R;
import com.zeewain.search.model.CommonSearchModel;

public class TipsItemViewHolder extends BaseViewHolder{
    public TextView textView;

    public TipsItemViewHolder(View view) {
        super(view);

        itemView = view;
        textView = view.findViewById(R.id.tv_item_search_tips);
    }

    @Override
    public void bindHolder(CommonSearchModel model) {
        textView.setText(model.getViewName());
    }
}
