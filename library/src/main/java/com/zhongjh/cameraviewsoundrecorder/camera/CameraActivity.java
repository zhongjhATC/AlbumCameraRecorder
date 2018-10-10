package com.zhongjh.cameraviewsoundrecorder.camera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.CameraSuccessListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DeviceUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.util.FileUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.widget.cameralayout.CameraLayout;

import java.io.File;

import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.BUTTON_STATE_BOTH;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.MEDIA_QUALITY_MIDDLE;

/**
 * 显示捕获镜头的界面
 */
public class CameraActivity extends AppCompatActivity {
    private CameraLayout cameraLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.fragment_camera_zjh);
        cameraLayout = findViewById(R.id.cameraLayout);

        // 定制参数
        cameraLayout.isMultiPicture(true);// 拍照是否允许拍多几张，只拍一张
        cameraLayout.setPictureMaxNumber(6);// 拍照是否允许拍多几张，只拍一张
        cameraLayout.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "ZhongjhCamera"); // 设置视频保存路径
        cameraLayout.setFeatures(BUTTON_STATE_BOTH);
        cameraLayout.setTip("轻触拍照，长按摄像");
        cameraLayout.setMediaQuality(MEDIA_QUALITY_MIDDLE); // 录制视频比特率
        cameraLayout.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Log.i("CameraActivity", "camera error");
                Intent intent = new Intent();
                setResult(103, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(CameraActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
            }
        });
        // 监听
        cameraLayout.setCameraSuccessListener(new CameraSuccessListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
//                Log.i("JCameraView", "bitmap = " + bitmap.getWidth());
                String path = FileUtil.saveBitmap("JCamera", bitmap);
                Intent intent = new Intent();
                intent.putExtra("path", path);
                setResult(101, intent);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                String path = FileUtil.saveBitmap("JCamera", firstFrame);
                Log.i("CJT", "url = " + url + ", Bitmap = " + path);
                Intent intent = new Intent();
                intent.putExtra("path", path);
                setResult(101, intent);
                finish();
            }
        });

//        cameraLayout.setLeftClickListener(v -> CameraActivity.this.finish());
//        cameraLayout.setRightClickListener(v -> Toast.makeText(CameraActivity.this,"Right", Toast.LENGTH_SHORT).show());

        Log.i("CJT", DeviceUtil.getDeviceModel());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraLayout.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraLayout.onPause();
    }
}
