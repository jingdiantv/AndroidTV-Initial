package com.zeewain.search.viewholder;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.example.search.R;
import com.zeewain.search.model.CommonSearchModel;

public class BoardInputViewHolder extends BaseViewHolder {
    public TextView textView;

    public BoardInputViewHolder(View view) {
        super(view);

        itemView = view;
        textView = view.findViewById(R.id.tv_item_keyboard_input);
    }

    @Override
    public void bindHolder(CommonSearchModel model) {
        SpannableString ss = new SpannableString("输入首字母/全拼搜索");
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#6CEDFF"));
        ss.setSpan(foregroundColorSpan,2,8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setHint(ss);
        textView.setTextSize(16.0f);
        textView.setGravity(Gravity.CENTER);
    }
}
