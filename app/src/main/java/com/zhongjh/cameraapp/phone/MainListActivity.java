package com.zhongjh.cameraapp.phone;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.databinding.ActivityMainCustomCameraviewBinding;
import com.zhongjh.cameraapp.databinding.ActivityMainListBinding;
import com.zhongjh.cameraapp.phone.customlayout.MainCustomCameraLayoutActivity;

/**
 * list配置
 *
 * @author zhongjh
 * @date 2019/4/25
 */
public class MainListActivity extends AppCompatActivity {

    ActivityMainListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainListBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // 简单版
        mBinding.btnSimple.setOnClickListener(v -> MainSimpleActivity.newInstance(MainListActivity.this));

        mBinding.btnSuperSimple.setOnClickListener(v -> MainSuperSimpleActivity.newInstance(MainListActivity.this));

        // 配置版
        mBinding.btnConfigure.setOnClickListener(v -> MainActivity.newInstance(MainListActivity.this));

        // 多种样式版
        mBinding.btnTheme.setOnClickListener(v -> MainThemeActivity.newInstance(MainListActivity.this));

        // 默认有数据的
        mBinding.btnOpenSee.setOnClickListener(v -> MainSeeActivity.newInstance(MainListActivity.this));

        // 这是灵活配置能选择xx张图片,xx个视频，xx个音频的用法示例
        mBinding.btnUpperLimit.setOnClickListener(v -> MainUpperLimitActivity.newInstance(MainListActivity.this));

        // recyclerView版
        mBinding.btnRecyclerView.setOnClickListener(v -> RecyclerViewActivity.newInstance(MainListActivity.this));

        // 自定义CameraView
        mBinding.btnCustomCameraView.setOnClickListener(v -> MainCustomCameraViewActivity.newInstance(MainListActivity.this));

        // 自定义CameraLayout
        mBinding.btnCustomCameraLayout.setOnClickListener(v -> MainCustomCameraLayoutActivity.newInstance(MainListActivity.this));
    }

}
