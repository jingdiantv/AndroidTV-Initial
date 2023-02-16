package com.zwn.user.ui.mine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zeewain.base.utils.AutoUtils;
import com.zwn.user.R;
import com.zwn.user.adapter.MessageCenterAdapter;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.databinding.MineMsgFragmentBinding;
import com.zwn.user.ui.UserCenterViewModel;
import com.zwn.user.ui.UserCenterViewModelFactory;
import com.zwn.user.utils.CustomDialog;
import com.zwn.user.widget.CenterAlignImageSpan;

import java.util.Objects;

public class MessageCenterFragment extends Fragment {
    private final String TAG = "MessageCenterFragment";
    private Activity mActivity;
    private UserCenterViewModel mViewModel;
    private MineMsgFragmentBinding mBinding;
    private MessageCenterAdapter mAdapter;
    private int mImgRadius = 26;
    private boolean mOnDelete = false;
    private Notify notify = Notify.NOTIFY_APP;
    private boolean isNotifyAppNormal = true;
    private boolean isNotifyAllNormal = true;

    private final SpannableString mSpannableString = new SpannableString("按[icon]键编辑");

    public static MessageCenterFragment newInstance() {
        return new MessageCenterFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = MineMsgFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AutoUtils.auto(mBinding.getRoot());

        initData();
        initView();
        initListener();
        initObserve();
        mViewModel.getMessage();
    }

    private void initData() {
        mActivity = Objects.requireNonNull(getActivity());
        mViewModel = new ViewModelProvider(this, new UserCenterViewModelFactory(
                UserRepository.getInstance(), getContext())).get(UserCenterViewModel.class);

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenHeight = dm.heightPixels;
        mImgRadius = (int) ((screenHeight / 1080.f) * mImgRadius);

        int menuSize = (int) ((screenHeight / 1080.f) * 56);
        Drawable menuIcon = ContextCompat.getDrawable(mActivity, R.mipmap.icon_menu);
        assert menuIcon != null;
        menuIcon.setBounds(0, 0, menuSize, menuSize);
        CenterAlignImageSpan imgSpan = new CenterAlignImageSpan(menuIcon, ImageSpan.ALIGN_BASELINE);
        mSpannableString.setSpan(imgSpan, 1, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


    }

    private void initView() {
        mBinding.tvMineMsgTips.setText(mSpannableString);
        mBinding.btnMineMsgClear.setNextFocusLeftId(R.id.btn_mine_msg_clear);

        mBinding.lvMineMsg.setNextFocusUpId(R.id.lv_mine_msg);
        mBinding.lvMineMsg.setNextFocusRightId(R.id.lv_mine_msg);
        mBinding.lvMineMsg.setNextFocusDownId(R.id.lv_mine_msg);

        mBinding.clMineMsgNotiApp.setNextFocusUpId(R.id.cl_mine_msg_noti_app);
        mBinding.clMineMsgNotiApp.setNextFocusDownId(R.id.cl_mine_msg_noti_all);
        mBinding.clMineMsgNotiApp.setNextFocusRightId(R.id.cl_mine_msg_noti_app);

        mBinding.clMineMsgNotiAll.setNextFocusDownId(R.id.cl_mine_msg_del);
        mBinding.clMineMsgNotiAll.setNextFocusUpId(R.id.cl_mine_msg_noti_app);
        mBinding.clMineMsgNotiAll.setNextFocusRightId(R.id.cl_mine_msg_noti_all);

        mBinding.clMineMsgDel.setNextFocusUpId(R.id.cl_mine_msg_noti_all);
        mBinding.clMineMsgDel.setNextFocusRightId(R.id.cl_mine_msg_del);
    }

    private void initListener() {
        mBinding.lvMineMsg.setOnItemClickListener((parent, view, position, id) -> {
            if (mOnDelete) {
                String showTitle = "确定删除" + mViewModel.pMsgList.get(position).title;
                new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                    mViewModel.delMessage(position);
                }, () -> {

                }).setTitle(showTitle).show();
            } else {
                Toast.makeText(getContext(), "点击了" + position, Toast.LENGTH_SHORT).show();
            }
        });

        mBinding.btnMineMsgClear.setOnClickListener(v -> {
            if (mOnDelete) {
                String showTitle = "确定清空全部通知";
                new CustomDialog(mActivity, R.layout.custom_dialog, () -> {
                    mViewModel.clearMessage();
                }, () -> {

                }).setTitle(showTitle).show();
            }
        });
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void initObserve() {
        mViewModel.pReqMsgState.observe(this, state -> {
            if (state.equals(UserCenterConf.REQ_SUCCESS)) {
                mAdapter = new MessageCenterAdapter(mViewModel.pMsgList, mImgRadius);
                mBinding.lvMineMsg.setAdapter(mAdapter);
                mBinding.clMineMsgNotiApp.requestFocus();
                mBinding.lvMineMsg.setVerticalScrollBarEnabled(false);
            }
        });

        mViewModel.pDelMsgState.observe(this, state -> {
            if (state.equals(UserCenterConf.REQ_SUCCESS)) {
                mAdapter.notifyDataSetChanged();
                if (mViewModel.pMsgList.size() == 0) {
                    mBinding.clMineMsgNothing.setVisibility(View.VISIBLE);
                    mBinding.btnMineMsgClear.setVisibility(View.INVISIBLE);
                    mOnDelete = false;
                    if (notify == Notify.NOTIFY_APP) {
                        mBinding.clMineMsgNotiApp.setBackgroundResource(R.drawable.selector_border_2_space_3_1);
                        mBinding.tvMineMsgNotiApp.setTextColor(getResources().getColorStateList(R.color.tv_color_1));
                        mBinding.clMineMsgNotiApp.requestFocus();
                        isNotifyAppNormal = true;
                    } else if (notify == Notify.NOTIFY_ALL) {
                        mBinding.clMineMsgNotiAll.setBackgroundResource(R.drawable.selector_border_2_space_3_1);
                        mBinding.tvMineMsgNotiAll.setTextColor(getResources().getColorStateList(R.color.tv_color_1));
                        mBinding.clMineMsgNotiAll.requestFocus();
                        isNotifyAllNormal = true;
                    }
                }
            }
        });
    }

    public int getMsgNum() {
        return mViewModel.pMsgList.size();
    }

    public void changeDelButtonState() {
        mOnDelete = !mOnDelete;
        mBinding.btnMineMsgClear.setVisibility(mOnDelete ? View.VISIBLE : View.INVISIBLE);
        mBinding.tvMineMsgTips.setVisibility(mOnDelete ? View.INVISIBLE : View.VISIBLE);
        mBinding.lvMineMsg.setNextFocusUpId(mOnDelete ? R.id.btn_mine_msg_clear : R.id.lv_mine_msg);
        mAdapter.isOnDelete(mOnDelete);
        mAdapter.notifyDataSetChanged();
    }

    public void moveRight() {
        if (mBinding.clMineMsgNotiApp.hasFocus()) {
            mBinding.clMineMsgNotiApp.setBackgroundResource(R.drawable.mine_msg_notify_cl_choose);
            mBinding.tvMineMsgNotiApp.setTextColor(ContextCompat.getColor(mActivity,
                    R.color.mine_msg_notify_tv_focus_color));
            mBinding.lvMineMsg.setNextFocusLeftId(R.id.cl_mine_msg_noti_app);
            notify = Notify.NOTIFY_APP;
            mBinding.lvMineMsg.setSelectionAfterHeaderView();
            mBinding.lvMineMsg.requestFocus();
            isNotifyAppNormal = false;
        }
        if (mBinding.clMineMsgNotiAll.hasFocus()) {
            mBinding.clMineMsgNotiAll.setBackgroundResource(R.drawable.mine_msg_notify_cl_choose);
            mBinding.tvMineMsgNotiAll.setTextColor(ContextCompat.getColor(mActivity,
                    R.color.mine_msg_notify_tv_focus_color));
            mBinding.lvMineMsg.setNextFocusLeftId(R.id.cl_mine_msg_noti_all);
            notify = Notify.NOTIFY_ALL;
            mBinding.lvMineMsg.setSelectionAfterHeaderView();
            mBinding.lvMineMsg.requestFocus();
            isNotifyAppNormal = false;
        }
        if (mBinding.clMineMsgDel.hasFocus()) {
            mBinding.lvMineMsg.setNextFocusLeftId(R.id.cl_mine_msg_noti_all);
            mBinding.lvMineMsg.setSelectionAfterHeaderView();
            mBinding.lvMineMsg.requestFocus();
            isNotifyAppNormal = false;
        }
        recover();
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    public void moveLeft() {
        if (mBinding.lvMineMsg.hasFocus()) {
            if (notify == Notify.NOTIFY_APP) {
                mBinding.clMineMsgNotiApp.setBackgroundResource(R.drawable.selector_border_2_space_3_1);
                mBinding.tvMineMsgNotiApp.setTextColor(getResources().getColorStateList(R.color.tv_color_1));
                isNotifyAppNormal = true;
            } else if (notify == Notify.NOTIFY_ALL) {
                mBinding.clMineMsgNotiAll.setBackgroundResource(R.drawable.selector_border_2_space_3_1);
                mBinding.tvMineMsgNotiAll.setTextColor(getResources().getColorStateList(R.color.tv_color_1));
                isNotifyAllNormal = true;
            }
        }
        recover();
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    public void moveUp() {
        if (mBinding.clMineMsgNotiAll.hasFocus()) {
            notify = Notify.NOTIFY_APP;
        }
        if (mBinding.clMineMsgDel.hasFocus()) {
            mBinding.clMineMsgNotiAll.setBackgroundResource(R.drawable.selector_border_2_space_3_1);
            mBinding.tvMineMsgNotiAll.setTextColor(getResources().getColorStateList(R.color.tv_color_1));
            isNotifyAllNormal = true;
        }
        recover();
    }

    public void moveDown() {
        if (mBinding.clMineMsgNotiApp.hasFocus()) {
            notify = Notify.NOTIFY_ALL;
        }
        if (mBinding.clMineMsgNotiAll.hasFocus()) {
            mBinding.clMineMsgNotiAll.setBackgroundResource(R.drawable.mine_msg_notify_cl_choose);
            mBinding.tvMineMsgNotiAll.setTextColor(ContextCompat.getColor(mActivity,
                    R.color.mine_msg_notify_tv_focus_color));
            isNotifyAllNormal = false;
        }
        recover();
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void recover() {
        Log.d(TAG, "mActivity.hasWindowFocus()" + mActivity.hasWindowFocus());
//        if (!mBinding.lvMineMsg.hasFocus() && !mBinding.btnMineMsgClear.hasFocus() && !mBinding.clMineMsgDel.hasFocus() && )
//        if (!isNotifyAppNormal) {
//            mBinding.clMineMsgNotiApp.setBackgroundResource(R.drawable.selector_border_2_space_3_1);
//            mBinding.tvMineMsgNotiApp.setTextColor(getResources().getColorStateList(R.color.tv_color_1));
//            isNotifyAppNormal = true;
//        }
//        if (!isNotifyAllNormal) {
//            mBinding.clMineMsgNotiAll.setBackgroundResource(R.drawable.selector_border_2_space_3_1);
//            mBinding.tvMineMsgNotiAll.setTextColor(getResources().getColorStateList(R.color.tv_color_1));
//            isNotifyAllNormal = true;
//        }
    }

    private enum Notify {
        NOTIFY_APP,
        NOTIFY_ALL
    }
}
