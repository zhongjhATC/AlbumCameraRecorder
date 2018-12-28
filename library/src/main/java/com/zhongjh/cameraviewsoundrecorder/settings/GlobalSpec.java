package com.zhongjh.cameraviewsoundrecorder.settings;

import android.content.pm.ActivityInfo;
import android.support.annotation.StyleRes;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.engine.ImageEngine;
import com.zhongjh.cameraviewsoundrecorder.album.engine.impl.GlideEngine;
import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnCheckedListener;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnSelectedListener;

import java.util.List;
import java.util.Set;

/**
 * 设置的一些属性,别的界面也根据这个来进行动态改变
 * Created by zhongjh on 2018/8/23.
 */
public class GlobalSpec {

    public AlbumSetting albumSetting;   // 相册的设置
    public CameraSetting cameraSetting; // 拍摄的设置
    public Set<MimeType> mimeTypeSet; // 选择 mime 的类型，MimeType.allOf()
    public boolean hasInited; // 是否通过正规方式进来
    @StyleRes
    public int themeId;         // 样式
    public int orientation;     // 旋转模式
    public int maxSelectable;   // 最大选择数量
    public CaptureStrategy captureStrategy; // 参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public ImageEngine imageEngine;

    private GlobalSpec() {
    }

    public static GlobalSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static GlobalSpec getCleanInstance() {
        GlobalSpec globalSpec = getInstance();
        globalSpec.reset();
        return globalSpec;
    }

    /**
     * 重置
     */
    private void reset() {
        cameraSetting = null;
        mimeTypeSet = null;
        themeId = R.style.AppTheme_Blue;
        orientation = 0;
        maxSelectable = 1;
        captureStrategy = null;
        hasInited = true;
        imageEngine = new GlideEngine();

    }

    /**
     * 是否需要旋转约束
     * @return 是否
     */
    public boolean needOrientationRestriction() {
        // SCREEN_ORIENTATION_UNSPECIFIED:未指定，此为默认值。由Android系统自己选择合适的方向
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }



    private static final class InstanceHolder {
        private static final GlobalSpec INSTANCE = new GlobalSpec();
    }


}
