package com.zeewain.base.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

/*拦截过快移动 支持边界向右向下找寻焦点*/
public class MyVerticalGridView extends VerticalGridView {
    private long cacheTime;
    private static final String TAG = "MyVerticalGridView";
    private int lastFocusPos = -1;
    private int numColumns = 1;
    private int tagPosition = -1;
    private int overNum = 0;
    private OnFocusLostListener mOnFocusLostListener;

    private final OnChildViewHolderSelectedListener myListener = new OnChildViewHolderSelectedListener() {
        @Override
        public void onChildViewHolderSelected(RecyclerView parent, ViewHolder child, int position, int subposition) {
            super.onChildViewHolderSelected(parent, child, position, subposition);
            Log.d(TAG, "onChildViewHolderSelected");
            lastFocusPos = position;
            int itemCount = getAdapter().getItemCount();
            overNum = itemCount % numColumns;
            if (itemCount > numColumns && overNum != 0) {
                tagPosition = itemCount - (itemCount % numColumns) - 1;
            } else {
                tagPosition = -1;
            }
            Log.d(TAG, "onChildViewHolderSelected: " + lastFocusPos);
        }

        @Override
        public void onChildViewHolderSelectedAndPositioned(RecyclerView parent, ViewHolder child, int position, int subposition) {
            super.onChildViewHolderSelectedAndPositioned(parent, child, position, subposition);
        }
    };

    @Override
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        super.setNumColumns(numColumns);
    }

    public MyVerticalGridView(Context context) {
        super(context);
        initView(context);
    }

    public MyVerticalGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MyVerticalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        Log.d(TAG, "MyVerticalGridView initView");
        addOnChildViewHolderSelectedListener(myListener);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View nextFocusView = FocusFinder.getInstance().findNextFocus(this, focused, direction);
        if (nextFocusView == null && lastFocusPos != -1 && lastFocusPos > tagPosition - (numColumns - overNum) && lastFocusPos <= tagPosition) {
            if (direction == View.FOCUS_DOWN) {
                int nextPos = getChildCount() - 1;
                return getChildAt(nextPos);
            }
        }
        if (nextFocusView == null && lastFocusPos != -1 && tagPosition != -1 && lastFocusPos == getAdapter().getItemCount() - 1) {
            if (direction == View.FOCUS_RIGHT) {
                // int nextPos = (getChildCount() / numColumns) * numColumns - 1;
                int nextPos = getChildCount() - 1;
                return getChildAt(nextPos);
            }
        }
        View view = super.focusSearch(focused, direction);
        if (view != null) {
            View nextFocusItemView = findContainingItemView(view);
            if (nextFocusItemView == null) {
                if (mOnFocusLostListener != null) {
                    mOnFocusLostListener.onFocusLost(direction);
                    return focused;
                }
            }
        }
        return view;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        boolean canScrollHorizontally = super.canScrollHorizontally(direction);
        Log.d(TAG, "canScrollHorizontally: " + canScrollHorizontally);
        return canScrollHorizontally;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        boolean canScrollVertically = super.canScrollVertically(direction);
        Log.d(TAG, "canScrollHorizontally: " + canScrollVertically);
        return canScrollVertically;
    }

    /**
     * 焦点快速移动拦截，限制300ms
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (action != KeyEvent.ACTION_DOWN) {
                    cacheTime = 0;
                    break;
                }
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - cacheTime >= 300) {
                    cacheTime = currentTimeMillis;
                    break;
                }
                return true;
        }

        return super.dispatchKeyEvent(event);
    }

    public void setOnFocusLostListener(OnFocusLostListener onFocusLostListener) {
        mOnFocusLostListener = onFocusLostListener;
    }

    public interface OnFocusLostListener {
        void onFocusLost(int direction);
    }
}