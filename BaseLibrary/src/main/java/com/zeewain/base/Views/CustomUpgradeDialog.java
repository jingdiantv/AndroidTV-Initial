package com.zeewain.base.Views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.zeewain.base.R;
import com.zeewain.base.utils.AutoUtils;

public class CustomUpgradeDialog extends Dialog {
    protected Context mContext;
    public TextView title;
    public Button cancel;
    public Button positive;
    private int layoutId;
    private OnClickListener mClickListener;
    public Button forceConfirm;

    public CustomUpgradeDialog(@NonNull Context context, int layoutId) {
        super(context, R.style.Translucent_NoTitle);
        this.mContext = context;
        this.layoutId = layoutId;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   setContentView(R.layout.layout_dialog);
        setContentView(layoutId);
        AutoUtils.auto(getWindow().getDecorView());
        title = findViewById(R.id.title);
        cancel = findViewById(R.id.cancel);
        positive = findViewById(R.id.positive);
        forceConfirm = findViewById(R.id.force_confirm);
        positive.requestFocus();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onCancel();
                    cancel();
                }

            }
        });
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onConFirm();
                    cancel();
                }
            }
        });
        forceConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.forceUpgrade();
                    cancel();
                }
            }
        });
        this.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                    if (mClickListener != null) {
                        mClickListener.forceExit();
                        return true;
                    }
                }
                return false;
            }
        });
    }


    private void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    public interface OnClickListener {

        void onConFirm();

        void onCancel();

        void forceUpgrade();

        void forceExit();
    }
}
