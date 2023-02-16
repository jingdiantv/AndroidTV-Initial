package com.zeewain.search.viewholder;

import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.example.search.R;
import com.zeewain.search.model.CommonSearchModel;

public class TipsTipsViewHolder extends BaseViewHolder{
    public TextView textView;

    public TipsTipsViewHolder(View view) {
        super(view);

        itemView = view;
        textView = view.findViewById(R.id.tv_item_search_tips_tips);
//        Typeface typeFace = Typeface.createFromAsset(view.getContext().getAssets(),"fonts/ZCOOLXiaoWei-Regular.ttf");
//        textView.setTypeface(typeFace);
    }

    @Override
    public void bindHolder(CommonSearchModel model) {
        textView.setText(Html.fromHtml(model.getViewName()));
    }
}
