package com.zhongjh.albumcamerarecorder.camera.util;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 有关摄像的工具类
 * Created by zhongjh on 2018/8/10.
 */
public class CameraParamUtil {

    private static final String TAG = "CameraParamUtil";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CameraParamUtil cameraParamUtil = null;

    private CameraParamUtil() {

    }

    public static CameraParamUtil getInstance() {
        if (cameraParamUtil == null) {
            cameraParamUtil = new CameraParamUtil();
            return cameraParamUtil;
        } else {
            return cameraParamUtil;
        }
    }

    /**
     * 获取预览的size
     *
     * @param list  获取的SupportedPreviewSizes分别是（width,height）:(1280,720),(960,720),(720,480),(640,480),(480,320),(320,240),(176,144)
     * @param width 标准宽
     * @param rate  高 / 宽的比例
     * @return 预览的size
     */
    public Camera.Size getPreviewSize(List<Camera.Size> list, int width, float rate) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width > width) && equalRate(s, rate)) {
                Log.i(TAG, "MakeSure Preview :w = " + s.width + " h = " + s.height);
                break;
            }
            // 不符合就+1
            i++;
        }
        if (i == list.size()) {
            // 如果全都不符合，就循环获取最接近的比例
            return getBestSize(list, rate);
        } else {
            // 返回符合的比例
            return list.get(i);
        }
    }

    /**
     * 获取拍照的size
     *
     * @param list 获取的SupportedPreviewSizes分别是（width,height）:(1280,720),(960,720),(720,480),(640,480),(480,320),(320,240),(176,144)
     * @param width 标准宽
     * @param rate 高 / 宽的比例
     * @return 拍照的size
     */
    public Camera.Size getPictureSize(List<Camera.Size> list, int width, float rate) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width > width) && equalRate(s, rate)) {
                Log.i(TAG, "MakeSure Picture :w = " + s.width + " h = " + s.height);
                break;
            }
            // 不符合就+1
            i++;
        }
        if (i == list.size()) {
            // 如果全都不符合，就循环获取最接近的比例
            return getBestSize(list, rate);
        } else {
            // 返回符合的比例
            return list.get(i);
        }
    }

    /**
     * 官方网站推荐的预览方向适配代码
     * 获取当前拍摄显示界面的角度
     *
     * @param context  上下文
     * @param cameraId 前摄像头 or 后摄像头
     * @return 当前拍摄显示界面的角度
     */
    public int getCameraDisplayOrientation(Context context, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // 获得当前屏幕方向
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        // 计算角度
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // 如果是前置摄像头，前置相机的orientation是270。前置摄像头作镜像翻转
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            // 如果是后置摄像头，后置相机的orientation是90
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /***
     * 判断是否有某某对焦模式
     * @param focusList 所有的对焦模式列表
     * @param focusMode 某某对焦模式
     * @return 是否有
     */
    public boolean isSupportedFocusMode(List<String> focusList, String focusMode) {
        for (int i = 0; i < focusList.size(); i++) {
            if (focusMode.equals(focusList.get(i))) {
                Log.i(TAG, "FocusMode supported " + focusMode);
                return true;
            }
        }
        Log.i(TAG, "FocusMode not supported " + focusMode);
        return false;
    }

    /**
     * 判断是否有某某图片模式
     * @param supportedPictureFormats 所有的图片模式列表
     * @param jpeg 某某图片模式
     * @return 是否有
     */
    public boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int jpeg) {
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            if (jpeg == supportedPictureFormats.get(i)) {
                Log.i(TAG, "Formats supported " + jpeg);
                return true;
            }
        }
        Log.i(TAG, "Formats not supported " + jpeg);
        return false;
    }

    /**
     * 循环获取最接近比例的size
     *
     * @param list 所有size
     * @param rate 当前屏幕的比例
     * @return 接近比例的size
     */
    private Camera.Size getBestSize(List<Camera.Size> list, float rate) {
        float previewDisparity = 100;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            Camera.Size cur = list.get(i);
            float prop = (float) cur.width / (float) cur.height;
            if (Math.abs(rate - prop) < previewDisparity) {
                previewDisparity = Math.abs(rate - prop);
                index = i;
            }
        }
        return list.get(index);
    }

    /**
     * 返回适合的比例
     *
     * @param size size的宽高
     * @param rate 当前屏幕的比例
     * @return 是否适合的比例
     */
    private boolean equalRate(Camera.Size size, float rate) {
        float r = (float) (size.width) / (float) (size.height);
        // 如果两者之间的距离小于等于0.2，就返回true
        return Math.abs(r - rate) <= 0.2;
    }

    /**
     * 排序，高的宽度排在前面
     */
    private class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }


}
