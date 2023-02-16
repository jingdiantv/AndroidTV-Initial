package com.zeewain.search.viewholder;

import android.view.View;
import android.widget.TextView;

import com.example.search.R;
import com.zeewain.search.model.CommonSearchModel;

public class BoardKeyViewHolder extends BaseViewHolder{
    public final TextView textView;

    public BoardKeyViewHolder(View view) {
        super(view);

        this.itemView = view;
        textView = itemView.findViewById(R.id.tv_item_keyboard_key);
    }

    @Override
    public void bindHolder(CommonSearchModel model) {
        textView.setText(model.getViewName());
    }
}
