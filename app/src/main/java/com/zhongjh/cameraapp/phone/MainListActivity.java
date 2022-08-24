package com.zhongjh.cameraapp.phone;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.cameraapp.R;
import com.zhongjh.cameraapp.configuration.Glide4Engine;
import com.zhongjh.cameraapp.databinding.ActivityMainListBinding;
import com.zhongjh.cameraapp.phone.customlayout.MainCustomCameraLayoutActivity;
import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MimeType;

import java.util.ArrayList;

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
        setContentView(R.layout.activity_main_list);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_list);

        // 简单版
        mBinding.btnSimple.setOnClickListener(v -> MainSimpleActivity.newInstance(MainListActivity.this));

        mBinding.btnSuperSimple.setOnClickListener(v -> MainSuperSimpleActivity.newInstance(MainListActivity.this));

        // 配置版
        mBinding.btnConfigure.setOnClickListener(v -> MainActivity.newInstance(MainListActivity.this));

        // 多种样式版
        mBinding.btnTheme.setOnClickListener(v -> MainThemeActivity.newInstance(MainListActivity.this));

        // 默认有数据的
        mBinding.btnOpenSee.setOnClickListener(v -> MainSeeActivity.newInstance(MainListActivity.this));

        // 独立预览相片功能
        mBinding.btnPreview.setOnClickListener(v -> {
            GlobalSetting globalSetting = MultiMediaSetting.from(MainListActivity.this).choose(MimeType.ofAll());
            globalSetting.allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "preview"));
            globalSetting.imageEngine(new Glide4Engine());
            ArrayList<Integer> list = new ArrayList<>();
            list.add(R.drawable.ic_camera_enhance_black_24dp);
            list.add(R.drawable.ic_play_arrow_white_24dp);
            globalSetting.openPreviewResourceId(MainListActivity.this, list, 0);
        });

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
