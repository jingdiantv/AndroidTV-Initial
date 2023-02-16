package com.zeewain.search.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.widget.Presenter;

public class KeyboardPresenter extends Presenter {

    private final static String TAG = "KeyboardPresenter";

    public final static int KEYBOARD_INPUT = 0;
    public final static int KEYBOARD_KEY = 1;
    public final static int KEYBOARD_FUNC = 2;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {

        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {

    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
