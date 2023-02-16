package com.zeewain.search.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.search.R;
import com.zeewain.search.adapter.SearchKeyboardAdapter;
import com.zeewain.search.conf.Constant;
import com.zeewain.search.model.CommonSearchModel;

/**
 * https://github.com/laoxiao79/TVEasyKeyboard
 */
public class CustomEasyTVKeyboard extends RelativeLayout {
    private static final String TAG = "CustomEasyTVKeyboard";

    private static final String[] mKeyList = {"A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O", "P","Q","R","S","T","U","V","W","X","Y","Z","1","2","3","4","5","6","7","8",
            "9","0"};
    private static final String[] mFuncList = {"清空全部", "删除"};

    private TextView mShowInputText;
    private FocusKeepRecyclerView mKeyboardView;

    private SearchKeyboardAdapter mAdapter;

    private SearchKeyboardAdapter.OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnMyTextChangedListener mOnMyTextChangedListener = null;

    private String mShowText = "";
    private int mMaxInputNum = 50;
    private Handler mHandler = new Handler();

    public CustomEasyTVKeyboard(Context context) {
        this(context, null);
    }

    public CustomEasyTVKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomEasyTVKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context);
        initListener();
    }

    public void initView(Context context) {
        View.inflate(context, R.layout.custom_input_board, this);

        mShowInputText = findViewById(R.id.tv_input_board_show_input);
        mKeyboardView = findViewById(R.id.rv_input_board_key);

        setHintText();

        GridLayoutManager layoutManager = new GridLayoutManager(context, 6);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position >= mKeyList.length) {
                    return 3;
                }
                return 1;
            }
        });
        mKeyboardView.setLayoutManager(layoutManager);

        int wideSpacingInPixels = getResources().getDimensionPixelSize(R.dimen.src_dp_10);
        int heightSpacingInPixels = getResources().getDimensionPixelSize(R.dimen.src_dp_16);

        mKeyboardView.addItemDecoration(new SpacesItemDecoration(wideSpacingInPixels, heightSpacingInPixels));

        mKeyboardView.setHasFixedSize(true);

        CommonSearchModel[] searchKeys = new CommonSearchModel[mKeyList.length + mFuncList.length];
        for (int i = 0; i < mKeyList.length; i++) {
            searchKeys[i] = new CommonSearchModel(SearchKeyboardAdapter.TYPE_KEY, mKeyList[i]);
        }
        for (int i = 0; i < mFuncList.length; i++) {
            searchKeys[mKeyList.length + i] = new CommonSearchModel(SearchKeyboardAdapter.TYPE_FUNC, mFuncList[i]);
        }
        mAdapter = new SearchKeyboardAdapter(searchKeys);
        mKeyboardView.setAdapter(mAdapter);
    }

    private void initListener() {
        mAdapter.setOnItemFocusListener((position, data) -> {
            if (myTextFocusListener != null) {
                Log.d(TAG, "焦点状态：" + data);
                myTextFocusListener.onFocusChanged(data);
            }
        });

        mOnItemClickListener = (pos, data) -> {
            boolean hasTextChange = true;
            if (pos < mKeyList.length) {
                if (mShowText.length() > mMaxInputNum) {
                    return;
                }
                mShowInputText.setGravity(Gravity.LEFT | Gravity.CENTER);
                mShowText = mShowText + data;
                mShowInputText.setText(mShowText);
            } else {
                if (mShowText.isEmpty()) {
                    hasTextChange = false;
                }
                if (data.equals(mFuncList[0])) {
                    mShowText = "";
                    mShowInputText.setText(mShowText);
                    mShowInputText.setGravity(Gravity.CENTER);
                } else if (data.equals(mFuncList[1])) {
                    deleteSearchText();
                }
            }
            if (hasTextChange) {
                mHandler.removeCallbacks(mRunnable);
                if (mShowText.isEmpty()) {
                    mHandler.post(mRunnable);
                } else {
                    mHandler.postDelayed(mRunnable, Constant.COMMON_DURATION);
                }
            }
        };
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }

    private final Runnable mRunnable = () -> mOnMyTextChangedListener.onTextChanged(mShowText);

    private void setHintText() {
        SpannableString ss = new SpannableString("输入首字母/全拼搜索");
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#6CEDFF"));
        ss.setSpan(foregroundColorSpan,2,8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mShowInputText.setHint(ss);
        mShowInputText.setGravity(Gravity.CENTER);
    }

    public OnMyTextFocusListener myTextFocusListener = null;

    public void setMyTextFocusListener(OnMyTextFocusListener myTextFocusListener) {
        this.myTextFocusListener = myTextFocusListener;
    }

    public interface OnMyTextFocusListener {
        void onFocusChanged(String text);
    }

    public interface OnMyTextChangedListener {
        void onTextChanged(String text);
    }

    public void setOnMyTextChangedListener(OnMyTextChangedListener mOnMyTextChangedListener) {
        this.mOnMyTextChangedListener = mOnMyTextChangedListener;
    }

    public void setFocusLostListener(FocusKeepRecyclerView.FocusLostListener listener) {
        mKeyboardView.setFocusLostListener(listener);
    }

    public void deleteSearchText() {
        if (mShowText != null) {
            int len = mShowText.length();
            if (len > 1) {
                mShowText = mShowText.substring(0, len - 1);
            } else {
                mShowText = "";
            }
        } else {
            mShowText = "";
        }
        mShowInputText.setText(mShowText);
        if (mShowText.isEmpty()) {
            mShowInputText.setGravity(Gravity.CENTER);
        }
    }

    public String getInputText() {
        return mShowText;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "返回键");
        }
        return super.dispatchKeyEvent(event);
    }
}