package com.zhongjh.cameraapp.phone;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.databinding.ActivityErrorBinding;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * @author zhongjh
 * @date 2022/6/2
 */
public class ErrorActivity extends AppCompatActivity {

    ActivityErrorBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_error);
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        // 获取所有的信息
        String errorDetails = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, getIntent());
        // 获取堆栈跟踪信息
        String stackTrace = CustomActivityOnCrash.getStackTraceFromIntent(getIntent());
        // 获取错误报告的Log信息
        String activityLog = CustomActivityOnCrash.getActivityLogFromIntent(getIntent());
        // 获得配置信息
        CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());
        mBinding.tvError.setText(
                "【errorDetails】\n" + errorDetails + "\n\n\n【stackTrace】\n" + stackTrace + "\n\n\n【activityLog】\n" + activityLog);
        mBinding.tvError.setTextColor(Color.BLUE);
        mBinding.btnRestart.setOnClickListener(v -> CustomActivityOnCrash.restartApplication(ErrorActivity.this, config));
    }
}
