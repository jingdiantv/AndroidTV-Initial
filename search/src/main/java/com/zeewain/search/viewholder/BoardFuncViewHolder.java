package com.zeewain.search.viewholder;

import android.view.View;
import android.widget.TextView;

import com.example.search.R;
import com.zeewain.search.model.CommonSearchModel;

public class BoardFuncViewHolder extends BaseViewHolder{
    public final TextView textView;

    public BoardFuncViewHolder(View view) {
        super(view);

        this.itemView = view;
        textView = view.findViewById(R.id.tv_item_keyboard_func);
    }

    @Override
    public void bindHolder(CommonSearchModel model) {
        textView.setText(model.getViewName());
    }
}
