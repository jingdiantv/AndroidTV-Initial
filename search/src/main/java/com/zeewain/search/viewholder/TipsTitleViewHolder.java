package com.zeewain.search.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.search.R;
import com.zeewain.search.adapter.SearchTipsAdapter;
import com.zeewain.search.model.CommonSearchModel;

public class TipsTitleViewHolder extends BaseViewHolder{
    public TextView textView;
    public ImageView imageView;

    public TipsTitleViewHolder(View view) {
        super(view);

        itemView = view;
        textView = view.findViewById(R.id.tv_item_search_tips_title);
        imageView = view.findViewById(R.id.iv_item_search_tips_title);
    }

    @Override
    public void bindHolder(CommonSearchModel model) {
        switch (model.getViewType()) {
            case SearchTipsAdapter.TYPE_TITLE_WITHOUT_DELETE:
                imageView.setVisibility(View.GONE);
                break;
            case SearchTipsAdapter.TYPE_TITLE_WITH_DELETE:
                break;
        }
        textView.setText(model.getViewName());
    }
}
