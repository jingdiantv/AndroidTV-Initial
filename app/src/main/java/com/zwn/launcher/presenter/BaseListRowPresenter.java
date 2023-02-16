package com.zwn.launcher.presenter;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.leanback.widget.BaseOnItemViewClickedListener;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.RowPresenter;

import com.zwn.launcher.R;


public class BaseListRowPresenter extends ListRowPresenter {

    public Animation mShakeX;

    public BaseListRowPresenter() {

    }

    public BaseListRowPresenter(int zoomFactorNone) {
        super(zoomFactorNone);
    }

    @Override
    protected void onRowViewAttachedToWindow(RowPresenter.ViewHolder vh) {
        super.onRowViewAttachedToWindow(vh);
        if (getOnItemViewClickedListener() != null) {
            vh.setOnItemViewClickedListener(getOnItemViewClickedListener());
        }
    }

    @Override
    protected void onRowViewDetachedFromWindow(RowPresenter.ViewHolder vh) {
        super.onRowViewDetachedFromWindow(vh);
        if (getOnItemViewClickedListener() != null) {
            vh.setOnItemViewClickedListener(null);
        }
    }

    private BaseOnItemViewClickedListener onItemViewClickedListener;

    public void setOnItemViewClickedListener(BaseOnItemViewClickedListener onItemViewClickedListener) {
        this.onItemViewClickedListener = onItemViewClickedListener;
    }

    public BaseOnItemViewClickedListener getOnItemViewClickedListener() {
        return onItemViewClickedListener;
    }

    public void shakeX(View view) {
        if (view != null) {
            if (mShakeX == null) {
                mShakeX = AnimationUtils.loadAnimation(view.getContext(), R.anim.host_shake);
            }
            view.clearAnimation();
            view.startAnimation(mShakeX);
        }
    }
}
