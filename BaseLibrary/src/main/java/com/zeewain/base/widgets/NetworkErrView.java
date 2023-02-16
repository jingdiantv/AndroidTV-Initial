package com.zeewain.base.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.R;

public class NetworkErrView extends ConstraintLayout {
    private ScaleConstraintLayout sclRetryBtn;
    private RetryClickListener retryClickListener = null;

    public NetworkErrView(@NonNull Context context) {
        this(context, null);
    }

    public NetworkErrView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkErrView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_network_err, this);
        sclRetryBtn = findViewById(R.id.scl_network_retry);
        sclRetryBtn.setOnClickListener(v -> {
            if(retryClickListener != null){
                retryClickListener.onRetryClick();
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){
            sclRetryBtn.requestFocus();
        }
    }

    public void setRetryClickListener(RetryClickListener retryClickListener) {
        this.retryClickListener = retryClickListener;
    }

    public interface RetryClickListener{
        void onRetryClick();
    }
}
