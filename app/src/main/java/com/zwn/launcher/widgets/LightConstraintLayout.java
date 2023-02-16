package com.zwn.launcher.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.zwn.launcher.R;

public class LightConstraintLayout extends ConstraintLayout implements View.OnFocusChangeListener {

    private ValueAnimator valueAnimator;
    private ImageView imageView;

    public LightConstraintLayout(Context context) {
        this(context, null);
    }

    public LightConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_light_constraint, this);
        imageView = findViewById(R.id.iv_light);
        setFocusable(true);
        setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v != null) {
            if (hasFocus) {
                move(imageView);
            } else {
                remove();
            }
        }
    }

    public void startAnimator(){
        move(imageView);
    }

    public void stopAnimator(){
        remove();
    }

    private void move(final View view) {
        view.bringToFront();
        final int width = getWidth();
        valueAnimator = ValueAnimator.ofFloat(((Integer) (-width -30)).floatValue(), ((Integer) (width + 30)).floatValue());
        valueAnimator.addUpdateListener(animation -> {
            float aFloat = (float) animation.getAnimatedValue();
            view.setTranslationX(aFloat);
            if(aFloat < -width*0.3 || aFloat > width*0.9){
                view.setAlpha(0);
            }else{
                view.setAlpha(0.5f);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        int divider = width / 355 - 1;
        float useDuration = 1000 * (divider * 0.25f + 1);
        valueAnimator.setDuration(((Float)useDuration).longValue());
        valueAnimator.setStartDelay(200);
        valueAnimator.start();
    }

    private void remove() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
}
