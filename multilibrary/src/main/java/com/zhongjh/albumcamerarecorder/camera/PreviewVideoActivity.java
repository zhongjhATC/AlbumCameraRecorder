package com.zhongjh.albumcamerarecorder.camera;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.widget.FullScreenVideoView;

/**
 * 一个预览合成分段录制的视频
 */
public class PreviewVideoActivity extends AppCompatActivity {

    FullScreenVideoView mVVPreview;
    ImageView mImgClose;
    Button mBtnConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);
    }

    /**
     * 初始化
     */
    private void initView() {
        mVVPreview = findViewById(R.id.vvPreview);
        mImgClose = findViewById(R.id.imgClose);
        mBtnConfirm = findViewById(R.id.btnConfirm);
    }


}
