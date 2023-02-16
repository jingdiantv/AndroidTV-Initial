package com.zwn.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zwn.user.ui.LoginCenterActivity;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    private View mAccountView;
    private View mInternetView;
    private View mBluetoothView;
    private View mImageView;
    private View mSoundView;
    private View mSystemView;

    private ImageView mImgIntro;
    private TextView mTextIntro;
    private TextView mUserAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_settings);

        initView();
        initClickListener();
        initFocusChangeListener();
    }

    private void initView() {
        mAccountView = (View) findViewById(R.id.include_set_acct);
        ((TextView) mAccountView.findViewById(R.id.tv_set_item_title)).setText("账号");
        mUserAccount =  ((TextView) mAccountView.findViewById(R.id.tv_set_item_cont));

        mInternetView = findViewById(R.id.include_set_net);
        ((TextView) mInternetView.findViewById(R.id.tv_set_item_title)).setText("网络");

        mBluetoothView = findViewById(R.id.include_set_bt);
        ((TextView) mBluetoothView.findViewById(R.id.tv_set_item_title)).setText("蓝牙");

        mImageView = findViewById(R.id.include_set_img);
        ((TextView) mImageView.findViewById(R.id.tv_set_item_title)).setText("图像");

        mSoundView = findViewById(R.id.include_set_sound);
        ((TextView) mSoundView.findViewById(R.id.tv_set_item_title)).setText("声音");

        mSystemView = findViewById(R.id.include_set_system);
        ((TextView) mSystemView.findViewById(R.id.tv_set_item_title)).setText("系统");

        mAccountView.requestFocus();

        mImgIntro = findViewById(R.id.iv_set_intro);
        mTextIntro = findViewById(R.id.tv_set_intro);
    }

    private void initClickListener() {
        mAccountView.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginCenterActivity.class);
            intent.putExtra("position",0);
            startActivity(intent);
        });

        mInternetView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "跳转异常", e);
                Toast.makeText(this, "无法跳转", Toast.LENGTH_SHORT).show();
            }
        });

        mBluetoothView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "跳转异常", e);
                Toast.makeText(this, "无法跳转", Toast.LENGTH_SHORT).show();
            }
        });

        mImageView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "跳转异常", e);
                Toast.makeText(this, "无法跳转", Toast.LENGTH_SHORT).show();
            }
        });

        mSoundView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "跳转异常", e);
                Toast.makeText(this, "无法跳转", Toast.LENGTH_SHORT).show();
            }
        });

        mSystemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "跳转异常", e);
                Toast.makeText(this, "无法跳转", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initFocusChangeListener() {
        mAccountView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mImgIntro.setImageResource(R.mipmap.icon_setting_head);
                mTextIntro.setText("登录账号玩转，万千互动应用");
            }
        });

        mInternetView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mImgIntro.setImageResource(R.mipmap.icon_setting_net);
                mTextIntro.setText("无线有线网络联结、网络状态");
            }
        });

        mBluetoothView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mImgIntro.setImageResource(R.mipmap.icon_setting_blue);
                mTextIntro.setText("蓝牙信息、已配对设备");
            }
        });

        mImageView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mImgIntro.setImageResource(R.mipmap.icon_setting_show);
                mTextIntro.setText("屏幕分辨率、图像模式");
            }
        });

        mSoundView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mImgIntro.setImageResource(R.mipmap.icon_setting_voice);
                mTextIntro.setText("音效模式、按键音设置");
            }
        });

        mSystemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mImgIntro.setImageResource(R.mipmap.icon_setting_system);
                mTextIntro.setText("软件更新、关于本机");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserAccount.setText(CommonUtils.getUserInfo());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}