package com.zwn.launcher;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.widgets.ScaleConstraintLayout;

public class ShowActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        int showActionCode = getIntent().getIntExtra(BaseConstants.EXTRA_SHOW_ACTION, -1);
        if(showActionCode == BaseConstants.ShowCode.CODE_CAMERA_ERROR){
            setContentView(R.layout.activity_show);
            TextView textView = findViewById(R.id.txt_show_tip);
            textView.setText("摄像头异常，请重新插拔USB摄像头或者重启设备！");
            ScaleConstraintLayout scaleConstraintLayout = findViewById(R.id.scl_confirm);
            scaleConstraintLayout.requestFocus();
            scaleConstraintLayout.setOnClickListener(v -> finish());
        }else{
            finish();
        }
    }
}