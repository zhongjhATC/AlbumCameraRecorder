package com.zhongjh.albumcamerarecorder.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.widget.FullScreenVideoView;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.BitmapUtils;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import gaode.zhongjh.com.common.utils.MediaStoreCompat;
import gaode.zhongjh.com.common.utils.StatusBarUtils;

import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_VIDEO;
import static com.zhongjh.albumcamerarecorder.constants.Constant.REQUEST_CODE_PREVIEW_VIDEO;

/**
 * 一个预览合成分段录制的视频
 * @author zhongjh
 */
public class PreviewVideoActivity extends AppCompatActivity {

    FullScreenVideoView mVideoViewPreview;
    ImageView mImgClose;
    Button mBtnConfirm;
    String mPath;
    File mFile;

    /**
     * 录像文件配置路径
     */
    private MediaStoreCompat mVideoMediaStoreCompat;

    /**
     * 打开activity
     *
     * @param fragment 打开者
     * @param path     视频地址
     */
    public static void startActivity(Fragment fragment, String path) {
        Intent intent = new Intent();
        intent.putExtra("path", path);
        intent.setClass(fragment.getContext(), PreviewVideoActivity.class);
        fragment.startActivityForResult(intent, REQUEST_CODE_PREVIEW_VIDEO);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtils.initStatusBar(PreviewVideoActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);
        mPath = getIntent().getStringExtra("path");
        initView();
        initListener();
        initData();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mVideoViewPreview = findViewById(R.id.vvPreview);
        mImgClose = findViewById(R.id.imgClose);
        mBtnConfirm = findViewById(R.id.btnConfirm);
    }

    private void initListener() {
        mBtnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent();
            // 加入视频到android系统库里面
            Uri mediaUri = BitmapUtils.displayToGallery(getApplicationContext(), mFile, TYPE_VIDEO, mVideoMediaStoreCompat.getSaveStrategy().directory, mVideoMediaStoreCompat);
            intent.putExtra("path", mPath);
            intent.putExtra("uri", mediaUri);
            setResult(RESULT_OK, intent);
            PreviewVideoActivity.this.finish();
        });
        mImgClose.setOnClickListener(v -> PreviewVideoActivity.this.finish());
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 公共配置
        GlobalSpec mGlobalSpec = GlobalSpec.getInstance();
        mVideoMediaStoreCompat = new MediaStoreCompat(PreviewVideoActivity.this);
        mVideoMediaStoreCompat.setSaveStrategy(mGlobalSpec.videoStrategy == null ? mGlobalSpec.saveStrategy : mGlobalSpec.videoStrategy);

        mFile = new File(mPath);
        playVideo(mFile);
    }

    /**
     * 播放视频,用于录制后，在是否确认的界面中，播放视频
     */
    private void playVideo(File file) {
        mVideoViewPreview.pause();
        // mediaController 是底部控制条
        MediaController mediaController = new MediaController(PreviewVideoActivity.this);
        mediaController.setAnchorView(mVideoViewPreview);
        mediaController.setMediaPlayer(mVideoViewPreview);
        mediaController.setVisibility(View.GONE);
        mVideoViewPreview.setMediaController(mediaController);
        Uri uri = Uri.fromFile(file);
        mVideoViewPreview.setVideoURI(uri);
        // 这段代码需要放在更新视频文件后播放，不然会找不到文件。
        mVideoViewPreview.setVisibility(View.VISIBLE);
        if (!mVideoViewPreview.isPlaying()) {
            mVideoViewPreview.start();
        }
        mVideoViewPreview.setOnCompletionListener(mediaPlayer -> {
            // 循环播放
            if (!mVideoViewPreview.isPlaying()) {
                mVideoViewPreview.start();
            }
        });
    }

}
