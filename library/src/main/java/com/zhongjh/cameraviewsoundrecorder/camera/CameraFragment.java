package com.zhongjh.cameraviewsoundrecorder.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.zhongjh.cameraviewsoundrecorder.MainActivity;
import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.CameraSuccessListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.PhotoVideoListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DeviceUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.widget.cameralayout.CameraLayout;
import com.zhongjh.cameraviewsoundrecorder.utils.DisplayMetricsUtils;

import java.io.File;

import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.BUTTON_STATE_BOTH;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.MEDIA_QUALITY_MIDDLE;

/**
 * 拍摄视频
 * Created by zhongjh on 2018/8/22.
 */
public class CameraFragment extends Fragment {

    protected Activity mActivity;

    private CameraLayout mCameraLayout;
    private String title;
    private int page;

    public static CameraFragment newInstance(int page, String title) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_zjh, container, false);
        // 隐藏状态栏
//        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mCameraLayout = view.findViewById(R.id.cameraLayout);

        // 定制参数
        mCameraLayout.isMultiPicture(true);// 拍照是否允许拍多几张，只拍一张
        mCameraLayout.setPictureMaxNumber(6);// 拍照是否允许拍多几张，只拍一张
        mCameraLayout.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "ZhongjhCamera"); // 设置视频保存路径
        mCameraLayout.setFeatures(BUTTON_STATE_BOTH);
        mCameraLayout.setTip("轻触拍照，长按摄像");
        mCameraLayout.setMediaQuality(MEDIA_QUALITY_MIDDLE); // 录制视频比特率
        mCameraLayout.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Log.i("CameraActivity", "camera error");
//                Intent intent = new Intent();
//                setResult(103, intent);
//                finish();
            }

            @Override
            public void AudioPermissionError() {
//                Toast.makeText(CameraActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
            }
        });
        // 监听
        mCameraLayout.setCameraSuccessListener(new CameraSuccessListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
//                Log.i("JCameraView", "bitmap = " + bitmap.getWidth());
//                String path = FileUtil.saveBitmap("JCamera", bitmap);
//                Intent intent = new Intent();
//                intent.putExtra("path", path);
//                setResult(101, intent);
//                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
//                String path = FileUtil.saveBitmap("JCamera", firstFrame);
//                Log.i("CJT", "url = " + url + ", Bitmap = " + path);
//                Intent intent = new Intent();
//                intent.putExtra("path", path);
//                setResult(101, intent);
//                finish();
            }
        });

        // 关闭
        mCameraLayout.setCloseListener(() -> mActivity.finish());

        // 拍摄按钮事件
        mCameraLayout.setPhotoVideoListener(new PhotoVideoListener() {
            @Override
            public void takePictures() {

            }

            @Override
            public void recordShort(long time) {

            }

            @Override
            public void recordStart() {
                // 母窗体禁止滑动
                ((MainActivity) mActivity).setTablayoutScroll(false);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraLayout.mViewHolder.pvLayout.getLayoutParams();
                layoutParams.bottomMargin = DisplayMetricsUtils.dip2px(50);//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
                mCameraLayout.mViewHolder.pvLayout.setLayoutParams(layoutParams);
            }

            @Override
            public void recordEnd(long time) {
                // 母船体启动滑动
                ((MainActivity) mActivity).setTablayoutScroll(true);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraLayout.mViewHolder.pvLayout.getLayoutParams();
                layoutParams.bottomMargin = 0;//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
                mCameraLayout.mViewHolder.pvLayout.setLayoutParams(layoutParams);
            }

            @Override
            public void recordZoom(float zoom) {

            }

            @Override
            public void recordError() {

            }
        });

//        mCameraLayout.setLeftClickListener(v -> mActivity.finish());
//        mCameraLayout.setRightClickListener(v -> {
////                Toast.makeText(CameraActivity.this,"Right", Toast.LENGTH_SHORT).show();
//        });

        Log.i("CJT", DeviceUtil.getDeviceModel());


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        // 全屏显示
//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getActivity().getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        } else {
//            View decorView = getActivity().getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(option);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraLayout.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraLayout.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mCameraLayout != null)
            if (isVisibleToUser)
                //相当于Fragment的onResume
                mCameraLayout.onResume();
            else
                //相当于Fragment的onPause
                mCameraLayout.onPause();
    }
}
