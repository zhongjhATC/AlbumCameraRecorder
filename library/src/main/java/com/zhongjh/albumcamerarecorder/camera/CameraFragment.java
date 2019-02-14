package com.zhongjh.albumcamerarecorder.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.CaptureListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.camera.listener.OperaeCameraListener;
import com.zhongjh.albumcamerarecorder.camera.util.DeviceUtil;
import com.zhongjh.albumcamerarecorder.utils.DisplayMetricsUtils;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;
import com.zhongjh.albumcamerarecorder.utils.constants.MultimediaTypes;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_UNDEFINED;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.MEDIA_QUALITY_MIDDLE;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION_PATH;

/**
 * 拍摄视频
 * Created by zhongjh on 2018/8/22.
 */
public class CameraFragment extends Fragment {

    private Activity mActivity;

    private CameraLayout mCameraLayout;

    public static CameraFragment newInstance() {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_zjh, container, false);
        // 隐藏状态栏
//        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mCameraLayout = view.findViewById(R.id.cameraLayout);
        mCameraLayout.setMediaQuality(MEDIA_QUALITY_MIDDLE); // 录制视频比特率
        mCameraLayout.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Log.i("CameraActivity", "camera error");
            }

            @Override
            public void AudioPermissionError() {
//                Toast.makeText(CameraActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
            }
        });

        // 关闭
        mCameraLayout.setCloseListener(() -> mActivity.finish());

        // 拍摄按钮事件
        mCameraLayout.setPhotoVideoListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                // 母窗体禁止滑动
                ViewBusinessUtils.setTablayoutScroll(false, ((MainActivity) mActivity), mCameraLayout.mViewHolder.pvLayout);
            }

            @Override
            public void onClick() {

            }

            @Override
            public void onLongClickShort(long time) {
                // 母窗体启动滑动
                ViewBusinessUtils.setTablayoutScroll(true, ((MainActivity) mActivity), mCameraLayout.mViewHolder.pvLayout);
            }

            @Override
            public void onLongClick() {
            }

            @Override
            public void onLongClickEnd(long time) {

            }

            @Override
            public void onLongClickZoom(float zoom) {

            }

            @Override
            public void onLongClickError() {

            }
        });

        // 确认取消事件
        mCameraLayout.setOperaeCameraListener(new OperaeCameraListener() {
            @Override
            public void cancel() {
                // 母窗体启动滑动
                ViewBusinessUtils.setTablayoutScroll(true, ((MainActivity) mActivity), mCameraLayout.mViewHolder.pvLayout);
            }

            @Override
            public void captureSuccess(ArrayList<String> paths) {
                Intent result = new Intent();
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, paths);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.PICTURE);
                mActivity.setResult(RESULT_OK, result);
                mActivity.finish();
            }

            @Override
            public void recordSuccess(String url) {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(url);
                //获取视频路径
                Intent result = new Intent();
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, arrayList);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.VIDEO);
                mActivity.setResult(RESULT_OK, result);
                mActivity.finish();
            }

        });

        // 拍摄后操作图片的事件
        mCameraLayout.setCaptureListener(new CaptureListener() {
            @Override
            public void remove(HashMap<Integer, BitmapData> captureBitmaps) {
                // 判断如果删除光图片的时候，母窗体启动滑动
                if (captureBitmaps.size() <= 0) {
                    ((MainActivity) mActivity).setTablayoutScroll(true);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraLayout.mViewHolder.pvLayout.getLayoutParams();
                    layoutParams.bottomMargin = 0;//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
                    mCameraLayout.mViewHolder.pvLayout.setLayoutParams(layoutParams);
                }
            }

            @Override
            public void add( HashMap<Integer, BitmapData> captureBitmaps) {
                if (captureBitmaps.size() > 0) {
                    // 母窗体禁止滑动
                    ((MainActivity) mActivity).setTablayoutScroll(false);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraLayout.mViewHolder.pvLayout.getLayoutParams();
                    layoutParams.bottomMargin = DisplayMetricsUtils.dip2px(50);//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
                    mCameraLayout.mViewHolder.pvLayout.setLayoutParams(layoutParams);
                }
            }
        });
        Log.i("CJT", DeviceUtil.getDeviceModel());
        return view;
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
