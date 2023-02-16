package com.zwn.user.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.zwn.user.R;

public class CustomDialog extends Dialog {

    private final int layoutId;
    private final OnConfirmListener confirmListener;
    private final OnCancelListener cancelListener;
    private String title;
    private Window window;

    public CustomDialog(@NonNull Context context, int layoutId, OnConfirmListener confirmListener,
                        OnCancelListener cancelListener) {
        super(context, R.style.dialog);
        window = getWindow();
        this.layoutId = layoutId;
        this.confirmListener = confirmListener;
        this.cancelListener = cancelListener;
    }

    public interface OnConfirmListener {
        void onConfirm();
    }

    public interface OnCancelListener {
        void onCancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        ((TextView) findViewById(R.id.dialog_text_title)).setText(title);
        findViewById(R.id.dialog_btn_confirm).setOnClickListener(v -> {
            confirmListener.onConfirm();
            cancel();
        });
        findViewById(R.id.dialog_btn_cancel).setOnClickListener(v -> {
            cancelListener.onCancel();
            cancel();
        });
        findViewById(R.id.dialog_btn_confirm).requestFocus();
    }

    public CustomDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * dialog 需要全屏的时候用，和clearFocusNotAle() 成对出现
     * 在show 前调用  focusNotAle   show后调用clearFocusNotAle
     *
     * @param window
     */
    private void focusNotAle(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /**
     * dialog 需要全屏的时候用，focusNotAle() 成对出现
     * 在show 前调用  focusNotAle   show后调用clearFocusNotAle
     *
     * @param window
     */
    private void clearFocusNotAle(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /**
     * 隐藏虚拟栏 ，显示的时候再隐藏掉
     *
     * @param window
     */
    public void hideNavigationBar(final Window window) {
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        //布局位于状态栏下方
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        //全屏
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        //隐藏导航栏
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                if (Build.VERSION.SDK_INT >= 19) {
                    uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                } else {
                    uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                }
                window.getDecorView().setSystemUiVisibility(uiOptions);
            }
        });
    }

    @Override
    public void show() {
        focusNotAle(window);
        super.show();
        hideNavigationBar(window);
        clearFocusNotAle(window);
    }
}
