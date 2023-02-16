package com.zeewain.base.widgets;

import static androidx.leanback.widget.FocusHighlight.ZOOM_FACTOR_NONE;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.R;


public class ScaleConstraintLayout extends ConstraintLayout implements View.OnFocusChangeListener {

    private MyFocusHighlightHelper.BrowseItemFocusHighlight mBrowseItemFocusHighlight;

    public ScaleConstraintLayout(Context context) {
        this(context, null);
    }

    public ScaleConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setClickable(true);
        setFocusableInTouchMode(true);
        setClipChildren(false);
        setClipToPadding(false);
        setOnFocusChangeListener(this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleConstraintLayout);
        int zoomIndex =  typedArray.getInteger(R.styleable.ScaleConstraintLayout_scale_mode, ZOOM_FACTOR_NONE);
        typedArray.recycle();
        if (mBrowseItemFocusHighlight == null) {
            mBrowseItemFocusHighlight =
                    new MyFocusHighlightHelper
                            .BrowseItemFocusHighlight(zoomIndex, false);
        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (mBrowseItemFocusHighlight != null) {
            mBrowseItemFocusHighlight.onItemFocused(v, hasFocus);
        }
    }
}
