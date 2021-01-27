package com.zhongjh.albumcamerarecorder.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.common.Constants;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.CaptureListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.camera.listener.OperaeCameraListener;
import com.zhongjh.albumcamerarecorder.camera.util.DeviceUtil;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_CHOICE;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION_PATH;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.REQUEST_CODE_PREVIEW_CAMRRA;

/**
 * 拍摄视频
 * Created by zhongjh on 2018/8/22.
 */
public class CameraFragment extends BaseFragment {


    private Activity mActivity;

    private CameraLayout mCameraLayout;

    //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private long mExitTime;

    public static CameraFragment newInstance() {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    @Override
    public void onAttach(@NotNull Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_zjh, container, false);

        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Toast.makeText(mActivity, "拍摄界面onBack", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        mCameraLayout = view.findViewById(R.id.cameraLayout);
        mCameraLayout.setFragment(this);
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
            public void captureSuccess(ArrayList<String> paths, ArrayList<Uri> uris) {
                Intent result = new Intent();
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, paths);
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, uris);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.PICTURE);
                result.putExtra(EXTRA_MULTIMEDIA_CHOICE, false);
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
                result.putExtra(EXTRA_MULTIMEDIA_CHOICE, false);
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
//                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraLayout.mViewHolder.pvLayout.getLayoutParams();
//                    layoutParams.bottomMargin = 0;//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
//                    mCameraLayout.mViewHolder.pvLayout.setLayoutParams(layoutParams);
                }
            }

            @Override
            public void add(HashMap<Integer, BitmapData> captureBitmaps) {
                if (captureBitmaps.size() > 0) {
                    // 母窗体禁止滑动
                    ((MainActivity) mActivity).setTablayoutScroll(false);
//                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraLayout.mViewHolder.pvLayout.getLayoutParams();
//                    layoutParams.bottomMargin = DisplayMetricsUtils.dip2px(50);//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
//                    mCameraLayout.mViewHolder.pvLayout.setLayoutParams(layoutParams);
                }
            }
        });
        Log.i("CJT", DeviceUtil.getDeviceModel());
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == REQUEST_CODE_PREVIEW_CAMRRA) {// 如果在预览界面点击了确定
            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                // 请求的预览界面
                Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
                // 获取选择的数据
                ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                if (selected == null)
                    return;
                // 循环判断，如果不存在，则删除
                ListIterator<Map.Entry<Integer, BitmapData>> i = new ArrayList<>(mCameraLayout.mCaptureBitmaps.entrySet()).listIterator(mCameraLayout.mCaptureBitmaps.size());
                while (i.hasPrevious()) {
                    Map.Entry<Integer, BitmapData> entry = i.previous();
                    int k = 0;
                    for (MultiMedia multiMedia : selected) {
                        if (!entry.getValue().getUri().toString().equals(multiMedia.getUri().toString())) {
                            k++;
                        }
                    }
                    if (k == selected.size()) {
                        // 所有都不符合，则删除
                        mCameraLayout.removePosition(entry.getKey());
                    }
                }

                // 刷新多个图片
                mCameraLayout.refreshMultiPhoto();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        // 判断当前状态是否休闲
        if (mCameraLayout.mState == Constants.STATE_PREVIEW) {
            return false;
        } else {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(mActivity.getApplicationContext(), "再按一次确认关闭", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraLayout != null)
            mCameraLayout.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraLayout != null)
            mCameraLayout.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraLayout != null)
            mCameraLayout.onDestroy();
    }

}
