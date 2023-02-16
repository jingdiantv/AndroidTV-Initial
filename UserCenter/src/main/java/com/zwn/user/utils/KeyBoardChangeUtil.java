package com.zwn.user.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class KeyBoardChangeUtil {

    private static volatile KeyBoardChangeUtil instance;
    private boolean lastKeyBoardOpen = false;
    private View decorView;
    private OnKeyBoardChangeListener keyBoardChangeListener;

    public static KeyBoardChangeUtil getInstance() {
        if (instance == null) {
            instance = new KeyBoardChangeUtil();
        }
        return instance;
    }

    public interface OnKeyBoardChangeListener{
        void onKeyBoardChange(boolean keyBoardOpen , int keyboardHeight);
    }

    public void addOnKeyBoardChangeListener(Activity activity, final OnKeyBoardChangeListener listener) {
        decorView = activity.getWindow().getDecorView();
        keyBoardChangeListener = listener;
        lastKeyBoardOpen = false;
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void removeOnKeyBoardChangeListener() {
        keyBoardChangeListener = null;
        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        decorView = null;
    }

    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            decorView.getWindowVisibleDisplayFrame(rect);
            // 计算出可见屏幕的高度
            int displayHight = rect.bottom - rect.top;
            // 获得屏幕整体的高度
            int hight = decorView.getHeight();
            // 获得键盘高度
            int keyboardHeight = hight - displayHight;
            boolean keyBoardOpen = (double) displayHight / hight < 0.8;
            if (keyBoardOpen != lastKeyBoardOpen) {
                keyBoardChangeListener.onKeyBoardChange(keyBoardOpen, keyboardHeight);
            }
            lastKeyBoardOpen = keyBoardOpen;
        }
    };
}
