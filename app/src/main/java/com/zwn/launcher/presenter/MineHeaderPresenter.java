package com.zwn.launcher.presenter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import com.zwn.launcher.R;
import com.zeewain.base.model.MineHeader;
import com.zwn.user.ui.LoginCenterActivity;

public class MineHeaderPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_header_account, parent, false);
        view.findViewById(R.id.cl_mine_header_acct).setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), LoginCenterActivity.class);
            view.getContext().startActivity(intent);
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof MineHeader) {
            ViewHolder vh = (ViewHolder) viewHolder;
            MineHeader header = (MineHeader) item;
            vh.account.setText(header.userAccount);
            vh.avatar.setBackgroundResource(header.userAvatar);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    private static class ViewHolder extends Presenter.ViewHolder {
        public ImageView avatar;
        public TextView account;

        @SuppressLint("SetTextI18n")
        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.iv_mine_header_avatar);
            account = view.findViewById(R.id.tv_mine_header_acct);
        }
    }
}
