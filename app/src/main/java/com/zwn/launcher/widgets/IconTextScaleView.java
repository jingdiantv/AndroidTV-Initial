package com.zwn.launcher.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.widgets.MyFocusHighlightHelper;
import com.zwn.launcher.R;

public class IconTextScaleView extends ConstraintLayout implements View.OnFocusChangeListener{
    private final Drawable iconBackgroundDrawable;
    private String iconText;
    private final boolean showTextFocusOnly;
    private TextView textView;
    private ImageView imageView;
    private MyFocusHighlightHelper.BrowseItemFocusHighlight mBrowseItemFocusHighlight;

    public IconTextScaleView(@NonNull Context context) {
        this(context, null);
    }

    public IconTextScaleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconTextScaleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setClickable(true);
        setFocusableInTouchMode(true);
        setClipChildren(false);
        setClipToPadding(false);
        setOnFocusChangeListener(this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconTextScaleView);
        iconBackgroundDrawable = typedArray.getDrawable(R.styleable.IconTextScaleView_icon_bg);
        iconText = typedArray.getString(R.styleable.IconTextScaleView_icon_text);
        showTextFocusOnly = typedArray.getBoolean(R.styleable.IconTextScaleView_show_text_focus_only, true);
        typedArray.recycle();
        if (mBrowseItemFocusHighlight == null) {
            mBrowseItemFocusHighlight =
                    new MyFocusHighlightHelper.BrowseItemFocusHighlight(2, false);
        }
        initView(context);
    }

    public void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_icon_text_scale_view, this);
        imageView = findViewById(R.id.iv_icon_text_scale);
        if(iconBackgroundDrawable != null){
            imageView.setBackground(iconBackgroundDrawable);
        }
        textView = findViewById(R.id.tv_icon_text_scale);
        if(iconText != null){
            textView.setText(iconText);
        }
        if(showTextFocusOnly){
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(showTextFocusOnly){
            if(hasFocus) {
                textView.setVisibility(View.VISIBLE);
            }else{
                textView.setVisibility(View.GONE);
            }
        }

        if (mBrowseItemFocusHighlight != null) {
            mBrowseItemFocusHighlight.onItemFocused(view, hasFocus);
        }
    }

    public void setImageBackground(Drawable iconBackgroundDrawable){
        imageView.setBackground(iconBackgroundDrawable);
    }

    public void setIconText(String text){
        iconText = text;
        textView.setText(iconText);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
