package com.zhongjh.cameraapp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.cameraapp.databinding.ActivityMainListBinding;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.util.ArrayList;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MimeType;

/**
 * list配置
 * Created by zhongjh on 2019/4/25.
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

        // 配置版
        mBinding.btnConfigure.setOnClickListener(v -> MainActivity.newInstance(MainListActivity.this));

        // 默认有数据的
        mBinding.btnOpenSee.setOnClickListener(v -> MainSeeActivity.newInstance(MainListActivity.this));

        // 独立预览相片功能
        mBinding.btnPreview.setOnClickListener(v -> {
            // 这个可以放在Application初始化类型、解析图片类，也可以不需要，但是如果你要单独使用预览图功能，必须提前设置这个
            MultiMediaSetting.init().choose(MimeType.ofAll()).imageEngine(new Glide4Engine());
            ArrayList<Integer> list = new ArrayList<>();
            list.add(R.drawable.ic_camera_enhance_black_24dp);
            list.add(R.drawable.ic_play_arrow_white_24dp);
            MultiMediaSetting.openPreviewResourceId(MainListActivity.this, list, 0);
        });
    }

}
